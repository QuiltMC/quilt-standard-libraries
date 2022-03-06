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

package org.quiltmc.qsl.model.api.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.util.InjectedInterface;

@Environment(EnvType.CLIENT)
@InjectedInterface(BakedModelManager.class)
public interface QuiltBakedModelManager {
	/**
	 * An alternative to {@link BakedModelManager#getModel(ModelIdentifier)} that accepts an
	 * {@link Identifier} instead.
	 *
	 * <p><b>This method, as well as its vanilla counterpart, should only be used after the
	 * {@link BakedModelManager} has completed reloading.</b> Otherwise, the result will be
	 * null or an old model.
	 *
	 * @param id the id of the model
	 * @return the model
	 */
	BakedModel getModel(Identifier id);
}
