/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.resource.Resource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.Structure;
import net.minecraft.test.StructureTestUtil;
import net.minecraft.util.Identifier;

@Mixin(StructureTestUtil.class)
public class StructureTestUtilMixin {
	private static final String GAME_TEST_STRUCTURE_PATH = "game_test/structures/";

	/**
	 * Replaces the default test structure loading with something that handles namespaces to work better with mods.
	 *
	 * @param id    the identifier of the structure
	 * @param world the world
	 * @param cir   the injection's callback info
	 */
	@Inject(
			method = "createStructure(Ljava/lang/String;Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/structure/Structure;",
			at = @At("HEAD"),
			cancellable = true
	)
	private static void createStructure(String id, ServerWorld world, CallbackInfoReturnable<Structure> cir) {
		var baseId = new Identifier(id);
		var structureId = new Identifier(baseId.getNamespace(), GAME_TEST_STRUCTURE_PATH + baseId.getPath() + ".snbt");

		try {
			Resource resource = world.getServer().getResourceManager().getResource(structureId).orElse(null);

			if (resource == null) {
				throw new RuntimeException("Unable to get resource: " + structureId);
			}

			String snbt;

			try (var inputStream = resource.open()) {
				snbt = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
			}

			NbtCompound nbt = NbtHelper.fromSnbt(snbt);
			Structure structure = world.getStructureTemplateManager().createStructureFromNbt(nbt);

			cir.setReturnValue(structure);
		} catch (IOException | CommandSyntaxException e) {
			throw new RuntimeException("Error while trying to load structure: " + structureId, e);
		}
	}
}
