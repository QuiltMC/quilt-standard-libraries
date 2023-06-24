/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.base.mixin;

import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.MinecraftServer;

import org.quiltmc.qsl.base.impl.QuiltBaseImpl;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
	@Shadow
	public abstract void stop(boolean shouldWait);

	@Unique
	private int quilt$autoTestTicks = 0;

	@Inject(method = "tick", at = @At("TAIL"))
	private void onEndTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		// Check whether we are in auto test mode, after the designated tick times we do an audit of Mixins then shutdown.
		if (QuiltBaseImpl.AUTO_TEST_SERVER_TICK_TIME != null) {
			this.quilt$autoTestTicks++;

			if (this.quilt$autoTestTicks == QuiltBaseImpl.AUTO_TEST_SERVER_TICK_TIME) {
				MixinEnvironment.getCurrentEnvironment().audit();
				this.stop(false);
			}
		}
	}
}
