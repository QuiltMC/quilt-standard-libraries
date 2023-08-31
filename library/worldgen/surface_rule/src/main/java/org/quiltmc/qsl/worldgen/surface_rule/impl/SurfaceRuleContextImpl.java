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

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.surfacebuilder.SurfaceRules;

import org.quiltmc.qsl.worldgen.surface_rule.api.SurfaceRuleContext;

@ApiStatus.Internal
public class SurfaceRuleContextImpl implements SurfaceRuleContext {
	private final SurfaceRules.SequenceMaterialRule materialRule;
	private final ResourceManager resourceManager;
	private final Identifier identifier;

	public SurfaceRuleContextImpl(SurfaceRules.MaterialRule rules, ResourceManager resourceManager, Identifier identifier) {
		this.materialRule = new SurfaceRules.SequenceMaterialRule(new ArrayList<>());
		this.materialRules().add(rules);
		this.resourceManager = resourceManager;
		this.identifier = identifier;
	}

	public SurfaceRules.MaterialRule freeze() {
		((QuiltSequenceMaterialRuleHooks) (Object) this.materialRule).quilt$freeze();
		return this.materialRule;
	}

	@Override
	public @NotNull List<SurfaceRules.MaterialRule> materialRules() {
		return this.materialRule.sequence();
	}

	@Override
	public @NotNull ResourceManager resourceManager() {
		return this.resourceManager;
	}

	@Override
	public @NotNull Identifier identifier() {
		return this.identifier;
	}

	public static class OverworldImpl extends SurfaceRuleContextImpl implements SurfaceRuleContext.Overworld {
		private final boolean surface;
		private final boolean bedrockRoof;
		private final boolean bedrockFloor;

		public OverworldImpl(boolean surface, boolean bedrockRoof, boolean bedrockFloor,
				SurfaceRules.MaterialRule rules, ResourceManager resourceManager, Identifier identifier) {
			super(rules, resourceManager, identifier);

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
	}

	@ApiStatus.Internal
	public static class NetherImpl extends SurfaceRuleContextImpl implements SurfaceRuleContext.Nether {
		public NetherImpl(SurfaceRules.MaterialRule rules, ResourceManager resourceManager, Identifier identifier) {
			super(rules, resourceManager, identifier);
		}
	}

	@ApiStatus.Internal
	public static class TheEndImpl extends SurfaceRuleContextImpl implements SurfaceRuleContext.TheEnd {
		public TheEndImpl(SurfaceRules.MaterialRule rules, ResourceManager resourceManager, Identifier identifier) {
			super(rules, resourceManager, identifier);
		}
	}
}
