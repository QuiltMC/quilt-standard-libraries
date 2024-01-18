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

import net.minecraft.advancement.criterion.CriterionTrigger;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class QuiltCriteria {
	public static <T extends CriterionTrigger<?>> T register(Identifier id, T criterion) {
		if ("minecraft".equals(id.getNamespace())) {
			throw new IllegalArgumentException("The " + id + " modded criterion trigger cannot be registered with the 'minecraft' namespace");
		}

		return Registry.register(Registries.TRIGGER_TYPE, id, criterion);
	}
}
