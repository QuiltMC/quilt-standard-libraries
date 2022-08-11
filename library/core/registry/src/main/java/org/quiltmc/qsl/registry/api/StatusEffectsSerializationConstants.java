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

package org.quiltmc.qsl.registry.api;

/**
 * Represents {@link net.minecraft.entity.effect.StatusEffect StatusEffects} serialization keys.
 * <p>
 * Quilt Registry patches some serialization methods to use full identifiers instead of raw numerical identifiers as those can cause issues
 * upon world save in a modded context.
 * Raw identifiers are still saved for Vanilla parity.
 */
public final class StatusEffectsSerializationConstants {
	/**
	 * Represents the effect identifier NBT key whose value is {@value}.
	 * <p>
	 * Used in the serialization of {@link net.minecraft.item.SuspiciousStewItem suspicious stew items},
	 * and of {@link net.minecraft.entity.passive.MooshroomEntity mooshroom entities}.
	 */
	public static final String EFFECT_ID_KEY = "quilt:effect_id";

	/**
	 * Represents the status effect instance identifier NBT key whose value is {@value}.
	 * <p>
	 * Used in the serialization of {@linkplain net.minecraft.entity.effect.StatusEffectInstance}.
	 */
	public static final String STATUS_EFFECT_INSTANCE_ID_KEY = "quilt:id";

	/**
	 * Represents the beacon primary effect key whose value is {@value}.
	 * <p>
	 * Used in the serialization of {@link net.minecraft.block.entity.BeaconBlockEntity beacon block entities}.
	 *
	 * @see #BEACON_SECONDARY_EFFECT_KEY
	 */
	public static final String BEACON_PRIMARY_EFFECT_KEY = "quilt:primary_effect";

	/**
	 * Represents the beacon secondary effect key whose value is {@value}.
	 * <p>
	 * Used in the serialization of {@link net.minecraft.block.entity.BeaconBlockEntity beacon block entities}.
	 *
	 * @see #BEACON_PRIMARY_EFFECT_KEY
	 */
	public static final String BEACON_SECONDARY_EFFECT_KEY = "quilt:secondary_effect";

	private StatusEffectsSerializationConstants() {
		throw new UnsupportedOperationException("StatusEffectsSerializationConstants only contains static definitions.");
	}
}
