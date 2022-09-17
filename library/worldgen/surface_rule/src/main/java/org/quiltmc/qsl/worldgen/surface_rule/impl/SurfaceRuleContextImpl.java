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

package org.quiltmc.qsl.worldgen.surface_rule.impl;

import java.util.List;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import net.minecraft.resource.ResourceManager;
import net.minecraft.world.gen.surfacebuilder.SurfaceRules;
import net.minecraft.world.gen.surfacebuilder.VanillaSurfaceRules;

import org.quiltmc.qsl.worldgen.surface_rule.api.SurfaceRuleContext;

@ApiStatus.Internal
public abstract class SurfaceRuleContextImpl extends ReloadableSequenceMaterialRule implements SurfaceRuleContext {
	SurfaceRules.MaterialRule vanillaRules;
	private ResourceManager resourceManager;

	public SurfaceRuleContextImpl(SurfaceRules.MaterialRule rules) {
		this.setup(rules);
	}

	private void setup(@NotNull SurfaceRules.MaterialRule rules) {
		this.materialRules().clear();
		this.vanillaRules = rules;
		this.materialRules().add(rules);
	}

	@Override
	public @NotNull List<SurfaceRules.MaterialRule> materialRules() {
		return this.sequence();
	}

	@Override
	public @NotNull ResourceManager resourceManager() {
		return this.resourceManager;
	}

	void reset(VanillaSurfaceRuleTracker<? extends SurfaceRuleContextImpl> tracker, ResourceManager resourceManager) {
		tracker.pause();
		this.setup(this.getVanillaRules());
		tracker.unpause();
		this.resourceManager = resourceManager;
	}

	void cleanup() {
		// No need to keep references lying around, especially if some are susceptible to being GC-ed.
		this.vanillaRules = null;
		this.resourceManager = null;
	}

	@ApiStatus.OverrideOnly
	protected abstract SurfaceRules.MaterialRule getVanillaRules();

	@ApiStatus.Internal
	public static class OverworldImpl extends SurfaceRuleContextImpl implements SurfaceRuleContext.Overworld {
		private final boolean surface;
		private final boolean bedrockRoof;
		private final boolean bedrockFloor;

		public OverworldImpl(boolean surface, boolean bedrockRoof, boolean bedrockFloor, SurfaceRules.MaterialRule rules) {
			super(rules);

			this.surface = surface;
			this.bedrockRoof = bedrockRoof;
			this.bedrockFloor = bedrockFloor;
		}

		@Override
		public boolean hasSurface() {
			return this.surface;
		}

		@Override
		public boolean hasBedrockRoof() {
			return this.bedrockRoof;
		}

		@Override
		public boolean hasBedrockFloor() {
			return this.bedrockFloor;
		}

		@Override
		protected SurfaceRules.MaterialRule getVanillaRules() {
			return VanillaSurfaceRules.getOverworldLikeRules(this.surface, this.bedrockRoof, this.bedrockFloor);
		}
	}

	@ApiStatus.Internal
	public static class NetherImpl extends SurfaceRuleContextImpl implements SurfaceRuleContext.Nether {
		public NetherImpl(SurfaceRules.MaterialRule rules) {
			super(rules);
		}

		@Override
		protected SurfaceRules.MaterialRule getVanillaRules() {
			return VanillaSurfaceRules.getNetherRules();
		}
	}

	@ApiStatus.Internal
	public static class TheEndImpl extends SurfaceRuleContextImpl implements SurfaceRuleContext.TheEnd {
		public TheEndImpl(SurfaceRules.MaterialRule rules) {
			super(rules);
		}

		@Override
		protected SurfaceRules.MaterialRule getVanillaRules() {
			return VanillaSurfaceRules.getEndRules();
		}
	}
}
