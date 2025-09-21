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

package org.quiltmc.qsl.entity.effect.impl;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.entity.effect.api.StatusEffectRemovalReason;

@ApiStatus.Internal
public final class QuiltStatusEffectInternals {
	private QuiltStatusEffectInternals() {
		throw new UnsupportedOperationException("QuiltStatusEffectInternals only contains static definitions.");
	}

	public static final String NAMESPACE = "quilt_status_effect";

	public static final int MIXIN_PRIORITY = 500;

	public static Identifier id(String path) {
		return Identifier.of(NAMESPACE, path);
	}

	public static final StatusEffectRemovalReason UNKNOWN_REASON = new StatusEffectRemovalReason(QuiltStatusEffectInternals.id("unknown"));
}
