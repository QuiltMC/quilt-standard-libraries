/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.registry.mixin.client;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.Item;
import org.quiltmc.qsl.registry.impl.sync.client.RebuildableIdModelHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ItemModels.class)
public abstract class ItemModelsMixin implements RebuildableIdModelHolder {
	@Shadow
	@Final
	public Int2ObjectMap<ModelIdentifier> modelIds;

	@Unique
	private Map<Item, ModelIdentifier> quilt$models = new Object2ObjectOpenHashMap<>();

	@Inject(method = "putModel", at = @At("HEAD"), cancellable = true)
	private void quilt$storeItem(Item item, ModelIdentifier modelId, CallbackInfo ci) {
		this.quilt$models.put(item, modelId);
	}

	@Override
	public void quilt$rebuildIds() {
		this.modelIds.clear();

		for (var entry : this.quilt$models.entrySet()) {
			this.modelIds.put(Item.getRawId(entry.getKey()), entry.getValue());
		}
	}
}
