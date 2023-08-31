/*
 * Copyright 2022 The Quilt Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.testing.mixin.command;

import java.util.Collection;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.dev.TestCommand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTestState;
import net.minecraft.test.TestFunction;
import net.minecraft.test.TestManager;
import net.minecraft.test.TestUtil;
import net.minecraft.text.Text;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import org.quiltmc.qsl.testing.impl.game.command.QuiltTestCommand;
import org.quiltmc.qsl.testing.impl.game.command.TestNameArgumentType;

@Mixin(TestCommand.class)
public class TestCommandMixin {
	@ModifyArg(
			method = "register",
			slice = @Slice(
					from = @At(value = "CONSTANT", args = "stringValue=export"),
					to = @At(value = "CONSTANT", args = "stringValue=pos")
			),
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/command/CommandManager;argument(Ljava/lang/String;Lcom/mojang/brigadier/arguments/ArgumentType;)Lcom/mojang/brigadier/builder/RequiredArgumentBuilder;"
			),
			index = 1
	)
	private static ArgumentType<String> quiltGameTest$replaceExportImportTestNameArgumentType(ArgumentType<String> name) {
		return new TestNameArgumentType();
	}

	@ModifyArg(
			method = "register",
			slice = @Slice(
					from = @At(value = "CONSTANT", args = "stringValue=export"),
					to = @At(value = "CONSTANT", args = "stringValue=exportthis")
			),
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/brigadier/builder/RequiredArgumentBuilder;executes(Lcom/mojang/brigadier/Command;)Lcom/mojang/brigadier/builder/ArgumentBuilder;"
			)
	)
	private static Command<ServerCommandSource> quiltGameTest$replaceExportCommand(Command<ServerCommandSource> original) {
		return context -> QuiltTestCommand.executeExport(context.getSource(), StringArgumentType.getString(context, "testName"));
	}

	@ModifyArg(
			method = "register",
			slice = @Slice(
					from = @At(value = "CONSTANT", args = "stringValue=exportthis"),
					to = @At(value = "CONSTANT", args = "stringValue=import")
			),
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;executes(Lcom/mojang/brigadier/Command;)Lcom/mojang/brigadier/builder/ArgumentBuilder;"
			)
	)
	private static Command<ServerCommandSource> quiltGameTest$replaceExportThisCommand(Command<ServerCommandSource> original) {
		return context -> QuiltTestCommand.executeExport(context.getSource());
	}

	@ModifyArg(
			method = "register",
			slice = @Slice(
					from = @At(value = "CONSTANT", args = "stringValue=create"),
					to = @At(value = "CONSTANT", args = "stringValue=width")
			),
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/command/CommandManager;argument(Ljava/lang/String;Lcom/mojang/brigadier/arguments/ArgumentType;)Lcom/mojang/brigadier/builder/RequiredArgumentBuilder;"
			),
			index = 1
	)
	private static ArgumentType<String> quiltGameTest$replaceCreateTestNameArgumentType(ArgumentType<String> name) {
		return new TestNameArgumentType();
	}

	@Redirect(
			method = "run(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/test/TestSet;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/StructureBlockBlockEntity;getStructurePath()Ljava/lang/String;")
	)
	private static String quiltGameTest$replaceStructurePathWithName(StructureBlockBlockEntity instance) {
		return instance.getStructureName();
	}

	@Redirect(
			method = "executeRun",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/test/TestUtil;startTest"
							+"(Lnet/minecraft/test/GameTestState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/test/TestManager;)V"
			)
	)
	private static void quiltGameTest$exceptionWrapRun(GameTestState test, BlockPos pos, TestManager testManager, ServerCommandSource source) {
		try {
			TestUtil.startTest(test, pos, testManager);
		} catch (Exception e) {
			source.sendError(Text.literal("Test run failed due to exception " + e + ". Please look at the logs for details."));
			e.printStackTrace();
		}
	}

	@Redirect(
			method = "run(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;II)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/test/TestUtil;runTestFunctions("
							+ "Ljava/util/Collection;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/BlockRotation;"
							+ "Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/test/TestManager;I"
							+ ")Ljava/util/Collection;"
			)
	)
	private static Collection<GameTestState> quiltGameTest$exceptionWrapRuns(
			Collection<TestFunction> testFunctions, BlockPos pos, BlockRotation rotation, ServerWorld world, TestManager testManager, int sizeZ,
			ServerCommandSource source
	) {
		try {
			return TestUtil.runTestFunctions(testFunctions, pos, rotation, world, testManager, sizeZ);
		} catch (Exception e) {
			source.sendError(Text.literal("Failed to run tests. Please look at the logs for details."));
			e.printStackTrace();
			throw e;
		}
	}

	@Redirect(
			method = {"executeImport"},
			at = @At(value = "NEW", target = "(Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/util/Identifier;"),
			expect = 2
	)
	private static Identifier quiltGameTest$fixStructureIdentifierImport(String namespace, String structure) {
		return new Identifier(structure);
	}

	@ModifyArg(
			method = "executeImport",
			at = @At(value = "INVOKE", target = "Ljava/nio/file/Paths;get(Ljava/lang/String;[Ljava/lang/String;)Ljava/nio/file/Path;"),
			index = 1
	)
	private static String[] quiltGameTest$fixImportPath(String[] more) {
		more[0] = more[0].replace(':', '/');
		return more;
	}

	@ModifyConstant(
			method = "onCompletion",
			constant = @Constant(stringValue = "All required tests passed :)")
	)
	private static String quiltGameTest$replaceSuccessMessage(String original) {
		// You may ask why, it's simple.
		// The original emoticon is a bit... weird.
		// And QSL members expressed some kind of interest into replacing it.
		// So here it is. I assure you this is a really necessary injection.
		return "All required tests passed :3c";
	}
}
