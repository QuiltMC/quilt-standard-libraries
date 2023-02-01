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

import com.google.gson.JsonObject;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.unmapped.C_xrtznmeb;
import net.minecraft.unmapped.C_zvuygmwb;

import org.quiltmc.qsl.recipe.api.serializer.QuiltRecipeSerializer;

/* a.k.a. LegacySmithingRecipeSerializerMixin */
@SuppressWarnings({"deprecated", "removal"})
@Mixin(C_xrtznmeb.C_exsoxfpi.class)
public abstract class C_exsoxfpiMixin implements QuiltRecipeSerializer<C_xrtznmeb> {
	@Override
	public JsonObject toJson(C_xrtznmeb recipe) {
		var accessor = (C_xrtznmebAccessor) recipe;

		return new C_zvuygmwb.C_zxdmndti(
				recipe.getId(),
				this,
				accessor.getBase(), accessor.getAddition(), recipe.getOutput(null).getItem(),
				null, null
		).toJson();
	}
}
