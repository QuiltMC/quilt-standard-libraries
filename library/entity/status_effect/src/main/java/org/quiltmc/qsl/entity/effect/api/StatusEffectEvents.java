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

package org.quiltmc.qsl.entity.effect.api;

import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.EventAwareListener;
import org.quiltmc.qsl.base.api.util.TriState;

/**
 * Events pertaining to status effects.
 */
public final class StatusEffectEvents {
	private StatusEffectEvents() {
		throw new UnsupportedOperationException("StatusEffectEvents only contains static definitions.");
	}

	/**
	 * An event that is called after a status effect is added.
	 */
	public static final Event<OnApplied> ON_APPLIED = Event.create(OnApplied.class, listeners -> (entity, effect, upgradeReapplying) -> {
		for (var listener : listeners) {
			listener.onApplied(entity, effect, upgradeReapplying);
		}
	});

	/**
	 * An event that is called when a status effect is about to be removed.
	 */
	public static final Event<ShouldRemove> SHOULD_REMOVE = Event.create(ShouldRemove.class, listeners -> (entity, effect, reason) -> {
		for (var listener : listeners) {
			var ret = listener.shouldRemove(entity, effect, reason);
			if (ret != TriState.DEFAULT) {
				return ret;
			}
		}

		return TriState.DEFAULT;
	});

	/**
	 * An event that is called after a status effect is removed.
	 */
	public static final Event<OnRemoved> ON_REMOVED = Event.create(OnRemoved.class, listeners -> (entity, effect, reason) -> {
		for (var listener : listeners) {
			listener.onRemoved(entity, effect, reason);
		}
	});

	/**
	 * Callback interface for {@link #ON_APPLIED}.
	 *
	 * @see #ON_APPLIED
	 */
	@FunctionalInterface
	public interface OnApplied extends EventAwareListener {
		/**
		 * Called after a status effect is added to an entity.
		 *
		 * @param entity            the entity that received the status effect
		 * @param effect            the status effect
		 * @param upgradeReapplying {@code true} if the status effect is being reapplied due to an upgrade, or {@code false} otherwise
		 */
		void onApplied(@NotNull LivingEntity entity, @NotNull StatusEffectInstance effect, boolean upgradeReapplying);
	}

	/**
	 * Callback interface for {@link #SHOULD_REMOVE}.
	 *
	 * @see #SHOULD_REMOVE
	 */
	@FunctionalInterface
	public interface ShouldRemove extends EventAwareListener {
		/**
		 * Checks if the status effect should be removed or not.
		 *
		 * @param entity the entity that has the status effect
		 * @param effect the status effect
		 * @param reason the reason the status effect should be removed
		 * @return {@link TriState#TRUE} if the status effect should be removed,
		 *         {@link TriState#FALSE} if the status effect to be kept,
		 *         or {@link TriState#DEFAULT} to let other listeners/the effect itself determine this.
		 */
		@NotNull TriState shouldRemove(@NotNull LivingEntity entity, @NotNull StatusEffectInstance effect, @NotNull StatusEffectRemovalReason reason);
	}

	/**
	 * Callback interface for {@link #ON_REMOVED}.
	 *
	 * @see #ON_REMOVED
	 */
	@FunctionalInterface
	public interface OnRemoved extends EventAwareListener {
		/**
		 * Called after a status effect is removed from an entity.
		 *
		 * @param entity the entity that had the status effect
		 * @param effect the status effect
		 * @param reason the reason the status effect was removed
		 */
		void onRemoved(@NotNull LivingEntity entity, @NotNull StatusEffectInstance effect, @NotNull StatusEffectRemovalReason reason);
	}
}
