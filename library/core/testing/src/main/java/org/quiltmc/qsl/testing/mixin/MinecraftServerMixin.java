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

package org.quiltmc.qsl.testing.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;

import org.quiltmc.qsl.testing.impl.game.QuiltGameTestImpl;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	/**
	 * Makes the tests tick.
	 *
	 * @return {@code true} if the tests should tick, or {@code false} otherwise
	 */
	@Redirect(
			method = "tickWorlds",
			at = @At(value = "FIELD", target = "Lnet/minecraft/SharedConstants;isDevelopment:Z")
	)
	private boolean onTickWorlds() {
		return SharedConstants.isDevelopment || QuiltGameTestImpl.ENABLED;
	}
}
