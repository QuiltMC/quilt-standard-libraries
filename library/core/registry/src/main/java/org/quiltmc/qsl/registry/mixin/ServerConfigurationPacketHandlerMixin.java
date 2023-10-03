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

package org.quiltmc.qsl.registry.mixin;

import java.util.Queue;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.ServerConfigurationPacketHandler;
import net.minecraft.network.configuration.ConfigurationTask;

import org.quiltmc.qsl.registry.impl.sync.server.FabricSyncTask;
import org.quiltmc.qsl.registry.impl.sync.server.QuiltSyncTask;
import org.quiltmc.qsl.registry.impl.sync.server.SetupSyncTask;
import org.quiltmc.qsl.registry.impl.sync.server.SyncTaskHolder;
import org.quiltmc.qsl.registry.impl.sync.server.ServerRegistrySync;

@Mixin(ServerConfigurationPacketHandler.class)
public abstract class ServerConfigurationPacketHandlerMixin implements AbstractServerPacketHandlerAccessor, SyncTaskHolder {
	@Shadow
	@Final
	private Queue<ConfigurationTask> tasks;

	@Shadow
	@Nullable
	private ConfigurationTask currentTask;

	@Shadow
	protected abstract void finishCurrentTask(ConfigurationTask.Type taskType);

	@SuppressWarnings("deprecated")
	@Inject(method = "addOptionalTasks", at = @At("TAIL"))
	private void quilt$addSyncTask(CallbackInfo ci) {
		if (ServerRegistrySync.shouldSync()) {
			this.tasks.add(new SetupSyncTask((ServerConfigurationPacketHandler) (Object) this));
		}
	}

	@Override
	public @Nullable QuiltSyncTask qsl$getQuiltSyncTask() {
		if (this.currentTask instanceof QuiltSyncTask task) return task;
		throw new IllegalStateException("Not currently in QuiltSyncTask!");
	}

	@Override
	public void qsl$finishQuiltSyncTask() {
		this.finishCurrentTask(QuiltSyncTask.TYPE);
	}

	@Override
	public void qsl$finishFabricSyncTask() {
		this.finishCurrentTask(FabricSyncTask.TYPE);
	}
}
