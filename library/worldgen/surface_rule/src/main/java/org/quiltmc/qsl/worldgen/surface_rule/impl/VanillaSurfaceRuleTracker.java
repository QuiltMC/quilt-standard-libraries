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

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.gen.surfacebuilder.SurfaceRules;

import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;
import org.quiltmc.qsl.worldgen.surface_rule.api.SurfaceRuleEvents;

@ApiStatus.Internal
public final class VanillaSurfaceRuleTracker<T extends SurfaceRuleContextImpl> {
	private static final Identifier SURFACE_RULES_APPLY_PHASE = new Identifier("quilt_surface_rule", "apply");
	public static final VanillaSurfaceRuleTracker<SurfaceRuleContextImpl.OverworldImpl> OVERWORLD = new VanillaSurfaceRuleTracker<>(
			context -> SurfaceRuleEvents.MODIFY_OVERWORLD.invoker().modifyOverworldRules(context)
	);
	public static final VanillaSurfaceRuleTracker<SurfaceRuleContextImpl.NetherImpl> NETHER = new VanillaSurfaceRuleTracker<>(
			context -> SurfaceRuleEvents.MODIFY_NETHER.invoker().modifyNetherRules(context)
	);
	public static final VanillaSurfaceRuleTracker<SurfaceRuleContextImpl.TheEndImpl> THE_END = new VanillaSurfaceRuleTracker<>(
			context -> SurfaceRuleEvents.MODIFY_THE_END.invoker().modifyTheEndRules(context)
	);

	private final Consumer<T> eventInvoker;
	private final ThreadLocal<Unit> paused = new ThreadLocal<>();
	private final Set<T> rules = Collections.newSetFromMap(new WeakHashMap<>());

	private VanillaSurfaceRuleTracker(Consumer<T> eventInvoker) {
		this.eventInvoker = eventInvoker;

		ResourceLoaderEvents.END_DATA_PACK_RELOAD.register(SURFACE_RULES_APPLY_PHASE, (server, resourceManager, error) -> {
			if (error == null && server == null) {
				this.init(resourceManager);
			}
		});
	}

	/**
	 * Called whenever we hit the point where we can start calling events.
	 * <p>
	 * This signifies we can start processing the Vanilla surface material rules.
	 */
	public void init(ResourceManager resourceManager) {
		this.rules.forEach(context -> this.apply(context, resourceManager));
	}

	public boolean isPaused() {
		return this.paused.get() == Unit.INSTANCE;
	}

	void pause() {
		this.paused.set(Unit.INSTANCE);
	}

	void unpause() {
		this.paused.remove();
	}

	/**
	 * Called whenever a Vanilla surface material rules are created and require processing.
	 *
	 * @param context the context
	 * @return the modded sequence material rule
	 */
	public SurfaceRules.MaterialRule modifyMaterialRules(T context) {
		this.rules.add(context);

		return context;
	}

	/**
	 * Triggers the modification event for the given surface material rules.
	 *
	 * @param context         the modification context
	 * @param resourceManager the resource manager
	 */
	private void apply(T context, ResourceManager resourceManager) {
		context.reset(this, resourceManager);

		this.eventInvoker.accept(context);

		context.cleanup();
	}
}
