/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.advancement.api;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.advancement.mixin.CriteriaAccessor;

public class QuiltCriteria {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static <T extends Criterion<?>> T register(Identifier id, T criterion) {
		if ("minecraft".equals(id.getNamespace())) {
			LOGGER.warn("An attempt to register a modded criteria with the vanilla id " + id + " was made");
		}

		if (CriteriaAccessor.values().containsKey(id)) {
			throw new IllegalArgumentException("Duplicate criterion id " + id);
		}

		CriteriaAccessor.values().put(id, criterion);
		return criterion;
	}
}
