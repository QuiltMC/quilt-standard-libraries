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

package org.quiltmc.qsl.testing.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.server.command.TestCommand;

@Mixin(TestCommand.class)
public class TestCommandMixin {
	@Redirect(
			method = "run(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/test/TestSet;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/StructureBlockBlockEntity;getStructurePath()Ljava/lang/String;")
	)
	private static String quiltGameTest$replaceStructurePathWithName(StructureBlockBlockEntity instance) {
		return instance.getStructureName();
	}
}
