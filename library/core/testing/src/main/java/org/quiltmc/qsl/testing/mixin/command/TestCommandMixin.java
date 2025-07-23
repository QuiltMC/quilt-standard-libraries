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

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.dev.TestCommand;

import org.quiltmc.qsl.testing.impl.game.command.QuiltTestCommand;

@Mixin(TestCommand.class)
public class TestCommandMixin {
	@ModifyArg(
			method = "register",
			slice = @Slice(
					from = @At(value = "CONSTANT", args = "stringValue=export"),
					to = @At(value = "CONSTANT", args = "stringValue=exportthese")
			),
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/brigadier/builder/RequiredArgumentBuilder;executes(Lcom/mojang/brigadier/Command;)Lcom/mojang/brigadier/builder/ArgumentBuilder;",
					remap = false
			)
	)
	private static Command<ServerCommandSource> quiltGameTest$replaceExportCommand(
			Command<ServerCommandSource> original
	) {
		return context -> QuiltTestCommand.executeExport(
				context.getSource(), StringArgumentType.getString(context, "testName")
		);
	}

	@ModifyArg(
			method = "register",
			slice = @Slice(
					from = @At(value = "CONSTANT", args = "stringValue=exportthese"),
					to = @At(value = "CONSTANT", args = "stringValue=exportthat")
			),
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;executes(Lcom/mojang/brigadier/Command;)Lcom/mojang/brigadier/builder/ArgumentBuilder;",
					remap = false
			)
	)
	private static Command<ServerCommandSource> quiltGameTest$replaceExportThisCommand(Command<ServerCommandSource> original) {
		return context -> QuiltTestCommand.executeExport(context.getSource());
	}

	// TODO find a solution... there's a possibility this isn't needed anymore.
	/*@Redirect(
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
	}*/
}
