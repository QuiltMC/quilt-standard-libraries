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

package org.quiltmc.qsl.loot.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.quiltmc.qsl.loot.api.QuiltLootTableBuilder;
import org.quiltmc.qsl.loot.api.event.LootTableLoadingCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(LootManager.class)
public class LootManagerMixin {
	@Shadow
	private Map<Identifier, LootTable> tables;

	@Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At("RETURN"))
	private void apply(Map<Identifier, JsonElement> map, ResourceManager manager, Profiler profiler, CallbackInfo ci) {
		Map<Identifier, LootTable> newTables = new HashMap<>();

		tables.forEach((id, table) -> {
			QuiltLootTableBuilder builder = QuiltLootTableBuilder.of(table);

			LootTableLoadingCallback.EVENT.invoker().onLootTableLoading(
					manager, (LootManager) (Object) this, id, builder, t -> newTables.put(id, t)
			);

			newTables.computeIfAbsent(id, i -> builder.build());
		});

		tables = ImmutableMap.copyOf(newTables);
	}
}
