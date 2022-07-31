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

package org.quiltmc.qsl.worldgen.surface_rule.api;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.EventAwareListener;

/**
 * Events relating to {@link net.minecraft.world.gen.surfacebuilder.SurfaceRules surface rules}.
 * <p>
 * <b>Modification events</b> like {@link #MODIFY_OVERWORLD}, {@link #MODIFY_NETHER}, and {@link #MODIFY_THE_END} allows to modify the surface rules
 * for the related Vanilla dimensions.
 */
public final class SurfaceRuleEvents {
	/**
	 * Represents the event phase named {@code quilt:remove} for the modification events for which removals may happen.
	 * <p>
	 * This phase always happen after the {@link Event#DEFAULT_PHASE default phase}.
	 */
	public static final Identifier REMOVE_PHASE = new Identifier("quilt", "remove");

	/**
	 * An event indicating that the surface rules for the Overworld dimension may get modified by mods, allowing the injection of modded surface rules.
	 */
	public static final Event<OverworldModifierCallback> MODIFY_OVERWORLD = Event.create(OverworldModifierCallback.class, callbacks -> context -> {
		for (var callback : callbacks) {
			callback.modifyOverworldRules(context);
		}
	});

	/**
	 * An event indicating that the surface rules for the Nether dimension may get modified by mods, allowing the injection of modded surface rules.
	 */
	public static final Event<NetherModifierCallback> MODIFY_NETHER = Event.create(NetherModifierCallback.class, callbacks -> context -> {
		for (var callback : callbacks) {
			callback.modifyNetherRules(context);
		}
	});

	/**
	 * An event indicating that the surface rules for the End dimension may get modified by mods, allowing the injection of modded surface rules.
	 */
	public static final Event<TheEndModifierCallback> MODIFY_THE_END = Event.create(TheEndModifierCallback.class, callbacks -> context -> {
		for (var callback : callbacks) {
			callback.modifyTheEndRules(context);
		}
	});

	@FunctionalInterface
	public interface OverworldModifierCallback extends EventAwareListener {
		/**
		 * Called to modify the given Overworld surface rules.
		 *
		 * @param context the modification context
		 */
		void modifyOverworldRules(@NotNull SurfaceRuleContext.Overworld context);
	}

	@FunctionalInterface
	public interface NetherModifierCallback extends EventAwareListener {
		/**
		 * Called to modify the given Nether surface rules.
		 *
		 * @param context the modification context
		 */
		void modifyNetherRules(@NotNull SurfaceRuleContext.Nether context);
	}

	@FunctionalInterface
	public interface TheEndModifierCallback extends EventAwareListener {
		/**
		 * Called to modify the given End surface rules.
		 *
		 * @param context the modification context
		 */
		void modifyTheEndRules(@NotNull SurfaceRuleContext.TheEnd context);
	}

	private SurfaceRuleEvents() {
		throw new UnsupportedOperationException("SurfaceMaterialRuleEvents only contains static definitions.");
	}

	static {
		MODIFY_OVERWORLD.addPhaseOrdering(Event.DEFAULT_PHASE, REMOVE_PHASE);
		MODIFY_NETHER.addPhaseOrdering(Event.DEFAULT_PHASE, REMOVE_PHASE);
		MODIFY_THE_END.addPhaseOrdering(Event.DEFAULT_PHASE, REMOVE_PHASE);
	}
}
