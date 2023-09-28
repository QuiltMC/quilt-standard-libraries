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

package org.quiltmc.qsl.advancement.testmod;

import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.advancement.criterion.ImpossibleCriterion;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.advancement.api.QuiltCriteria;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class CriteriaTestmod implements ModInitializer {
	public static final Criterion<ImpossibleCriterion.Conditions> IMPOSSIBLE = QuiltCriteria.register(new Identifier("quilt", "impossible"), new ImpossibleCriterion());

	@Override
	public void onInitialize(ModContainer mod) {
	}
}
