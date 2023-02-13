/*
 * Copyright 2022-2023 QuiltMC
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

import com.mojang.brigadier.arguments.ArgumentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.server.command.TestCommand;
import net.minecraft.util.Identifier;

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
			method = {"executeExport(Lnet/minecraft/server/command/ServerCommandSource;Ljava/lang/String;)I", "executeImport"},
			at = @At(value = "NEW", target = "(Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/util/Identifier;"),
			expect = 2
	)
	private static Identifier quiltGameTest$fixStructureIdentifierExportImport(String namespace, String structure) {
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

	@ModifyArg(
			method = "executeExport(Lnet/minecraft/server/command/ServerCommandSource;Ljava/lang/String;)I",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/data/dev/NbtProvider;convertNbtToSnbt(Lnet/minecraft/unmapped/C_temnquoh;Ljava/nio/file/Path;Ljava/lang/String;Ljava/nio/file/Path;)Ljava/nio/file/Path;"
			),
			index = 2
	)
	private static String quiltGameTest$fixImportPath(String structure) {
		return structure.replace(':', '/');
	}
}
