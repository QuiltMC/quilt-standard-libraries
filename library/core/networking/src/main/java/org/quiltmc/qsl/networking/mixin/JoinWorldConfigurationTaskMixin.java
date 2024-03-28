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

package org.quiltmc.qsl.networking.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.ServerConfigurationPacketHandler;
import net.minecraft.network.configuration.ConfigurationTask;
import net.minecraft.network.configuration.JoinWorldConfigurationTask;
import net.minecraft.network.packet.Packet;

import org.quiltmc.qsl.networking.api.ServerConfigurationNetworking;
import org.quiltmc.qsl.networking.api.ServerConfigurationTaskManager;
import org.quiltmc.qsl.networking.impl.server.ServerConfigurationPacketHandlerKnowingTask;
import org.quiltmc.qsl.networking.impl.server.ServerNetworkingImpl;
import org.quiltmc.qsl.networking.mixin.accessor.ServerConfigurationPacketHandlerAccessor;

@Mixin(JoinWorldConfigurationTask.class)
public abstract class JoinWorldConfigurationTaskMixin implements ConfigurationTask, ServerConfigurationPacketHandlerKnowingTask {

	@Unique
	private ServerConfigurationPacketHandler handler;

	@Inject(method = "start", at = @At("HEAD"), cancellable = true)
	private void ensureNoTasksLeft(Consumer<Packet<?>> task, CallbackInfo ci) {
		if (!((ServerConfigurationPacketHandlerAccessor) this.handler).getTasks().isEmpty()) {
			// TODO: Throw this error?
			// throw new RuntimeException("Not all tasks have been completed by the configuration handler!");
			ServerNetworkingImpl.getAddon(handler).logger.error("Not all tasks have been completed by the configuration handler! Tasks remaining: {}", ((ServerConfigurationPacketHandlerAccessor) this.handler).getTasks());
			((ServerConfigurationTaskManager) this.handler).addTask(this);
			((ServerConfigurationTaskManager) this.handler).finishTask(JoinWorldConfigurationTask.TYPE);
			ci.cancel();
		}
	}

	@Override
	public void setServerConfigurationPacketHandler(ServerConfigurationPacketHandler handler) {
		this.handler = handler;
	}
}
