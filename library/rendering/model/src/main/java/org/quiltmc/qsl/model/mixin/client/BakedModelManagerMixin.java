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

package org.quiltmc.qsl.model.mixin.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.model.api.client.QuiltBakedModelManager;

@Mixin(BakedModelManager.class)
public class BakedModelManagerMixin implements QuiltBakedModelManager {
	@Shadow
	private Map<Identifier, BakedModel> models;
	@Shadow
	private BakedModel missingModel;

	@Override
	public BakedModel getModel(Identifier id) {
		return models.getOrDefault(id, missingModel);
	}
}
