/*
 * Copyright 2022-2023 QuiltMC
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

package org.quiltmc.qsl.recipe.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.recipe.Ingredient;
import net.minecraft.unmapped.C_xrtznmeb;

/* a.k.a. LegacySmithingRecipeAccessor */
@SuppressWarnings({"deprecated", "removal"})
@Mixin(C_xrtznmeb.class)
public interface C_xrtznmebAccessor {
	// Gets base
	@Accessor("f_mihfbkri")
	Ingredient getBase();

	// Gets addition
	@Accessor("f_cilfmlnk")
	Ingredient getAddition();
}
