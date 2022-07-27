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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.world.gen.surfacebuilder.SurfaceRules;

import org.quiltmc.qsl.worldgen.surface_rule.api.SurfaceRuleEvents;

@ApiStatus.Internal
public final class VanillaSurfaceRuleTracker<T extends SurfaceRuleContextImpl> {
	public static final VanillaSurfaceRuleTracker<SurfaceRuleContextImpl.OverworldImpl> OVERWORLD =
			new VanillaSurfaceRuleTracker<>(context -> SurfaceRuleEvents.MODIFY_OVERWORLD.invoker().modifyOverworldRules(context));
	public static final VanillaSurfaceRuleTracker<SurfaceRuleContextImpl> NETHER = new VanillaSurfaceRuleTracker<>(
			context -> SurfaceRuleEvents.MODIFY_NETHER.invoker().modifyNetherRules(context)
	);
	public static final VanillaSurfaceRuleTracker<SurfaceRuleContextImpl> THE_END = new VanillaSurfaceRuleTracker<>(
			context -> SurfaceRuleEvents.MODIFY_THE_END.invoker().modifyTheEndRules(context)
	);

	private final Consumer<T> eventInvoker;
	private List<T> rules = new ArrayList<>();

	private VanillaSurfaceRuleTracker(Consumer<T> eventInvoker) {
		this.eventInvoker = eventInvoker;
	}

	/**
	 * Called whenever we hit the point where we can start calling events.
	 * <p>
	 * This signifies we can start processing the Vanilla surface material rules.
	 */
	public void init() {
		this.rules.forEach(this::apply);
		this.rules = null;
	}

	/**
	 * Called whenever a Vanilla surface material rules are created and require processing.
	 *
	 * @param context the context
	 * @return the modded sequence material rule
	 */
	public SurfaceRules.SequenceMaterialRule modifyMaterialRules(T context) {
		if (this.rules == null) {
			// We got past the bootstrap phase, we can process directly.
			this.apply(context);
		} else {
			// We are still in the bootstrap phase, keep this for later.
			this.rules.add(context);
		}

		return context.getSequenceRule();
	}

	/**
	 * Triggers the modification event for the given surface material rules.
	 *
	 * @param context the modification context
	 */
	private void apply(T context) {
		this.eventInvoker.accept(context);

		context.freeze();
	}
}
