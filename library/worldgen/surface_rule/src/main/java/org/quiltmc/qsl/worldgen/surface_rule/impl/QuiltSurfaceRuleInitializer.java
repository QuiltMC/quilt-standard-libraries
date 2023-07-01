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

package org.quiltmc.qsl.worldgen.surface_rule.impl;

import java.util.Set;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.ResourceManager;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.surfacebuilder.SurfaceRules;

import org.quiltmc.qsl.registry.api.event.DynamicRegistryManagerSetupContext;
import org.quiltmc.qsl.registry.api.event.RegistryEntryContext;
import org.quiltmc.qsl.registry.api.event.RegistryEvents;
import org.quiltmc.qsl.worldgen.surface_rule.api.SurfaceRuleEvents;
import org.quiltmc.qsl.worldgen.surface_rule.mixin.ChunkGeneratorSettingsAccessor;

@ApiStatus.Internal
public class QuiltSurfaceRuleInitializer implements RegistryEvents.DynamicRegistrySetupCallback {
	@Override
	public void onDynamicRegistrySetup(@NotNull DynamicRegistryManagerSetupContext context) {
		context.withRegistries(registryMap -> {
			SurfaceRuleEvents.MODIFY_OVERWORLD_DATA.update(context.resourceManager());
			SurfaceRuleEvents.MODIFY_NETHER_DATA.update(context.resourceManager());
			SurfaceRuleEvents.MODIFY_THE_END_DATA.update(context.resourceManager());
			SurfaceRuleEvents.MODIFY_GENERIC_DATA.update(context.resourceManager());
		}, Set.of(RegistryKeys.CHUNK_GENERATOR_SETTINGS));
		context.monitor(RegistryKeys.CHUNK_GENERATOR_SETTINGS, monitor -> {
			monitor.forAll(ctx -> this.modifyChunkGeneratorSettings(ctx, context.resourceManager()));
		});
	}

	private void modifyChunkGeneratorSettings(RegistryEntryContext<ChunkGeneratorSettings> context, ResourceManager resourceManager) {
		var baseSurfaceRule = context.value().surfaceRule();

		SurfaceRuleContextImpl globalImpl;
		if (context.id().equals(ChunkGeneratorSettings.OVERWORLD.getValue())
				|| context.id().equals(ChunkGeneratorSettings.AMPLIFIED.getValue())
				|| context.id().equals(ChunkGeneratorSettings.LARGE_BIOMES.getValue())) {
			globalImpl = this.modifyOverworld(true, false, true,
					baseSurfaceRule, resourceManager, context);
		} else if (context.id().equals(ChunkGeneratorSettings.CAVES.getValue())) {
			globalImpl = this.modifyOverworld(false, true, true,
					baseSurfaceRule, resourceManager, context);
		} else if (context.id().equals(ChunkGeneratorSettings.FLOATING_ISLANDS.getValue())) {
			globalImpl = this.modifyOverworld(false, false, false,
					baseSurfaceRule, resourceManager, context);
		} else if (context.id().equals(ChunkGeneratorSettings.NETHER.getValue())) {
			var impl = new SurfaceRuleContextImpl.NetherImpl(baseSurfaceRule, resourceManager, context.id());
			SurfaceRuleEvents.MODIFY_NETHER.invoker().modifyNetherRules(impl);
			globalImpl = impl;
		} else if (context.id().equals(ChunkGeneratorSettings.END.getValue())) {
			var impl = new SurfaceRuleContextImpl.TheEndImpl(baseSurfaceRule, resourceManager, context.id());
			SurfaceRuleEvents.MODIFY_THE_END.invoker().modifyTheEndRules(impl);
			globalImpl = impl;
		} else {
			globalImpl = new SurfaceRuleContextImpl(baseSurfaceRule, resourceManager, context.id());
			SurfaceRuleEvents.MODIFY_GENERIC.invoker().modifyGenericSurfaceRules(globalImpl);
		}

		((ChunkGeneratorSettingsAccessor) (Object) context.value()).setSurfaceRule(globalImpl.freeze());
	}

	private SurfaceRuleContextImpl modifyOverworld(boolean surface, boolean bedrockRoof, boolean bedrockFloor,
			SurfaceRules.MaterialRule baseSurfaceRule, ResourceManager resourceManager, RegistryEntryContext<ChunkGeneratorSettings> context) {
		var impl = new SurfaceRuleContextImpl.OverworldImpl(surface, bedrockRoof, bedrockFloor,
				baseSurfaceRule, resourceManager, context.id());
		SurfaceRuleEvents.MODIFY_OVERWORLD.invoker().modifyOverworldRules(impl);
		return impl;
	}
}
