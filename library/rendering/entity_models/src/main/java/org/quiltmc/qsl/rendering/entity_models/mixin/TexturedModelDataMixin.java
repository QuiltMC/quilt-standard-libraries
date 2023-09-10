/*
 * Copyright 2023 QuiltMC
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

package org.quiltmc.qsl.rendering.entity_models.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.model.TexturedModelData;

import org.quiltmc.qsl.rendering.entity_models.api.model.TypedModel;
import org.quiltmc.qsl.rendering.entity_models.api.model.ModelType;
import org.quiltmc.qsl.rendering.entity_models.api.model.ModelTypes;

@Mixin(TexturedModelData.class)
public class TexturedModelDataMixin implements TypedModel {
	@Unique
	private ModelType quilt$modelType = ModelTypes.QUILT_MODEL;

	@Override
	public ModelType getType() {
		return this.quilt$modelType;
	}

	@Override
	public void setType(ModelType type) {
		this.quilt$modelType = type;
	}
}
