/*
 * Copyright 2023 The Quilt Project
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

import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.DataFixer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.HolderProvider;
import net.minecraft.resource.ResourceManager;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.storage.WorldSaveStorage;

import org.quiltmc.qsl.testing.impl.game.QuiltGameTestStructureLoader;

@Mixin(StructureTemplateManager.class)
public abstract class StructureTemplateManagerMixin {
	@Shadow
	private ResourceManager resourceManager;

	@Shadow
	public abstract Structure createStructureFromNbt(NbtCompound nbt);

	@Unique
	private Optional<Structure> quilt$createGameTestStructure(Identifier id) {
		return QuiltGameTestStructureLoader.loadStructure(this.resourceManager, id).map(this::createStructureFromNbt);
	}

	@Unique
	private Stream<Identifier> quilt$streamGameTestStructure() {
		return QuiltGameTestStructureLoader.streamTemplatesFromResource(this.resourceManager);
	}

	/**
	 * Injects a new test structure loading logic that handles namespaces to work better with mods.
	 *
	 * @param resourceManager the resource manager
	 * @param session         the storage session
	 * @param dataFixer       the data fixer
	 * @param blockRegistry   access to the block registry
	 * @param ci              the callback info
	 * @param builder         the structure source list builder
	 */
	@Inject(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Lcom/google/common/collect/ImmutableList$Builder;build()Lcom/google/common/collect/ImmutableList;",
					remap = false
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void quilt$addGameTestTemplateProvider(ResourceManager resourceManager, WorldSaveStorage.Session session, DataFixer dataFixer,
			HolderProvider<Block> blockRegistry, CallbackInfo ci,
			ImmutableList.Builder<StructureTemplateManager.Source> builder) {
		builder.add(new StructureTemplateManager.Source(this::quilt$createGameTestStructure, this::quilt$streamGameTestStructure));
	}
}
