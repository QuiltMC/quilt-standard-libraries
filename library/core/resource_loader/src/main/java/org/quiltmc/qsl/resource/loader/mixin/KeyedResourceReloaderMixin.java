/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.resource.loader.mixin;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.loot.LootManager;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.function.FunctionLoader;
import net.minecraft.tag.TagManagerLoader;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;
import org.quiltmc.qsl.resource.loader.api.reloader.ResourceReloaderKeys;

@Mixin({
		RecipeManager.class, ServerAdvancementLoader.class, FunctionLoader.class,
		LootManager.class, TagManagerLoader.class
})
public abstract class KeyedResourceReloaderMixin implements IdentifiableResourceReloader {
	@Unique
	private Identifier quilt$id;
	@Unique
	private Collection<Identifier> quilt$dependencies;

	@Override
	@SuppressWarnings({"ConstantConditions", "RedundantCast"})
	public Identifier getQuiltId() {
		if (this.quilt$id == null) {
			Object self = this;

			if (self instanceof RecipeManager) {
				this.quilt$id = ResourceReloaderKeys.Server.RECIPES;
			} else if (self instanceof ServerAdvancementLoader) {
				this.quilt$id = ResourceReloaderKeys.Server.ADVANCEMENTS;
			} else if (self instanceof FunctionLoader) {
				this.quilt$id = ResourceReloaderKeys.Server.FUNCTIONS;
			} else if (self instanceof LootManager) {
				this.quilt$id = ResourceReloaderKeys.Server.LOOT_TABLES;
			} else if (self instanceof TagManagerLoader) {
				this.quilt$id = ResourceReloaderKeys.Server.TAGS;
			} else {
				this.quilt$id = new Identifier("private/" + self.getClass().getSimpleName().toLowerCase(Locale.ROOT));
			}
		}

		return this.quilt$id;
	}

	@Override
	@SuppressWarnings({"ConstantConditions", "RedundantCast"})
	public Collection<Identifier> getQuiltDependencies() {
		if (this.quilt$dependencies == null) {
			Object self = this;

			if (self instanceof TagManagerLoader) {
				this.quilt$dependencies = Collections.emptyList();
			} else {
				this.quilt$dependencies = Collections.singletonList(ResourceReloaderKeys.Server.TAGS);
			}
		}

		return this.quilt$dependencies;
	}
}
