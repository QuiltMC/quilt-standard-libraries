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

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.collection.IdList;

import org.quiltmc.qsl.registry.impl.sync.SynchronizedIdList;
import org.quiltmc.qsl.registry.impl.sync.client.RebuildableIdModelHolder;

@Environment(EnvType.CLIENT)
@Mixin(ItemColors.class)
public class ItemColorsMixin implements RebuildableIdModelHolder {
	@Final
	@Shadow
	private IdList<ItemColorProvider> providers;

	@Unique
	private final Map<Item, ItemColorProvider> quilt$providers = new Object2ObjectOpenHashMap<>();


	@Inject(method = "register", at = @At("TAIL"))
	private void quilt$storeProviders(ItemColorProvider provider, ItemConvertible[] items, CallbackInfo ci) {
		for (var item : items) {
			this.quilt$providers.put(item.asItem(), provider);
		}
	}

	@Override
	public void quilt$rebuildIds() {
		SynchronizedIdList.clear(this.providers);

		for (var entry : this.quilt$providers.entrySet()) {
			this.providers.set(entry.getValue(), Item.getRawId(entry.getKey()));
		}
	}
}
