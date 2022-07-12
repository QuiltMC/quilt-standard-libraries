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

package org.quiltmc.qsl.multipart.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.server.world.ThreadedAnvilChunkStorage;

import org.quiltmc.qsl.multipart.api.EntityPart;

@Mixin(ThreadedAnvilChunkStorage.class)
public class ThreadedAnvilChunkStorageMixin {
	@Redirect(method = "loadEntity", at = @At(value = "CONSTANT", args = "classValue=net/minecraft/entity/boss/dragon/EnderDragonPart", ordinal = 0))
	private static Class<?> cancelEnderDragonCheck(Object targetObject, Class<?> classValue) {
		return EntityPart.class;
	}
}
