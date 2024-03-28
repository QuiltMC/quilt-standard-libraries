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

package org.quiltmc.qsl.networking.mixin;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.ServerConfigurationPacketHandler;
import net.minecraft.network.configuration.ConfigurationTask;
import net.minecraft.network.configuration.JoinWorldConfigurationTask;
import net.minecraft.network.listener.AbstractServerPacketHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.unmapped.C_eyqfalbd;

import org.quiltmc.qsl.networking.api.ServerConfigurationTaskManager;
import org.quiltmc.qsl.networking.impl.DisconnectPacketSource;
import org.quiltmc.qsl.networking.impl.NetworkHandlerExtensions;
import org.quiltmc.qsl.networking.impl.server.SendChannelsTask;
import org.quiltmc.qsl.networking.impl.server.ServerConfigurationNetworkAddon;
import org.quiltmc.qsl.networking.impl.server.ServerConfigurationPacketHandlerKnowingTask;

// We want to apply a bit earlier than other mods which may not use us in order to prevent refCount issues
@Mixin(value = ServerConfigurationPacketHandler.class, priority = 900)
abstract class ServerConfigurationPacketHandlerMixin extends AbstractServerPacketHandler implements NetworkHandlerExtensions, DisconnectPacketSource, ServerConfigurationTaskManager {
	@Mutable
	@Shadow
	@Final
	private Queue<ConfigurationTask> tasks;
	@Shadow
	@Nullable
	private ConfigurationTask currentTask;

	@Shadow
	protected abstract void finishCurrentTask(ConfigurationTask.Type taskType);

	@Shadow
	public abstract void startConfiguration();

	@Shadow
	protected abstract void startNextTask();

	@Unique
	private ServerConfigurationNetworkAddon addon;

	@Unique
	private boolean sentConfiguration = false;

	@Unique
	private ConfigurationTask immediateTask = null;

	@Unique
	private Queue<ConfigurationTask> priorityTasks = new ConcurrentLinkedQueue<>();

	ServerConfigurationPacketHandlerMixin(MinecraftServer server, ClientConnection connection, C_eyqfalbd c_eyqfalbd) {
		super(server, connection, c_eyqfalbd);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void initAddon(CallbackInfo ci) {
		this.addon = new ServerConfigurationNetworkAddon((ServerConfigurationPacketHandler) (Object) this, this.server);
		// A bit of a hack but it allows the field above to be set in case someone registers handlers during INIT event which refers to said field
		this.addon.lateInit();
	}

	@Inject(method = "startConfiguration", at = @At("HEAD"), cancellable = true)
	private void start(CallbackInfo ci) {
		if (!this.sentConfiguration) {
			this.addImmediateTask(new SendChannelsTask(this.addon));

			this.addPriorityTask(new ConfigurationTask() {
				static final Type TYPE = new Type("minecraft:start_configuration");

				@Override
				public void start(Consumer<Packet<?>> task) {
					ServerConfigurationPacketHandlerMixin.this.startConfiguration();
					ServerConfigurationPacketHandlerMixin.this.finishTask(TYPE);
				}

				@Override
				public Type getType() {
					return TYPE;
				}
			});

			this.sentConfiguration = true;
			this.startNextTask();
			ci.cancel();
		}
	}

	@WrapWithCondition(method = "startConfiguration", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ServerConfigurationPacketHandler;startNextTask()V"))
	private boolean doNotCallStart(ServerConfigurationPacketHandler handler) {
		return false;
	}

	@ModifyReceiver(method = "startNextTask", at = @At(value = "INVOKE", target = "Ljava/util/Queue;poll()Ljava/lang/Object;"))
	private Queue<ConfigurationTask> modifyTaskQueue(Queue<ConfigurationTask> originalQueue) {
		if (this.immediateTask != null) {
			var singleQueue = new ConcurrentLinkedQueue<ConfigurationTask>();
			singleQueue.add(this.immediateTask);
			this.immediateTask = null;
			return singleQueue;
		}

		if (!this.priorityTasks.isEmpty()) {
			return this.priorityTasks;
		}

		return originalQueue;
	}

	@WrapOperation(method = "startConfiguration", at = @At(value = "NEW", target = "()Lnet/minecraft/network/configuration/JoinWorldConfigurationTask;"))
	private JoinWorldConfigurationTask setHandlerStart(Operation<JoinWorldConfigurationTask> constructor) {
		var task = constructor.call();
		((ServerConfigurationPacketHandlerKnowingTask) task).setServerConfigurationPacketHandler((ServerConfigurationPacketHandler) (Object) this);
		return task;
	}

	@WrapOperation(method = "rejoinWorld", at = @At(value = "NEW", target = "()Lnet/minecraft/network/configuration/JoinWorldConfigurationTask;"))
	private JoinWorldConfigurationTask setHandlerRejoin(Operation<JoinWorldConfigurationTask> constructor) {
		var task = constructor.call();
		((ServerConfigurationPacketHandlerKnowingTask) task).setServerConfigurationPacketHandler((ServerConfigurationPacketHandler) (Object) this);
		return task;
	}

	@Inject(method = "startNextTask", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/configuration/ConfigurationTask;start(Ljava/util/function/Consumer;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
	public void beforeTaskStart(CallbackInfo ci, ConfigurationTask task) {
		this.addon.logger.debug("Starting task with type: {}", task.getType());
	}

	@Inject(method = "finishCurrentTask", at = @At("HEAD"))
	public void beforeTaskStart(ConfigurationTask.Type type, CallbackInfo ci) {
		this.addon.logger.debug("Finishing task type: {}", type);
	}

	@Inject(method = "onDisconnected", at = @At("HEAD"))
	private void handleDisconnection(Text reason, CallbackInfo ci) {
		this.addon.handleDisconnect();
	}

	@Override
	public ServerConfigurationNetworkAddon getAddon() {
		return this.addon;
	}

	@Override
	public Packet<?> createDisconnectPacket(Text message) {
		return new DisconnectS2CPacket(message);
	}

	@Override
	public void addTask(ConfigurationTask task) {
		this.addon.logger.debug("Adding task with type: {}", task.getType());
		this.tasks.add(task);
	}

	@Override
	public void addPriorityTask(ConfigurationTask task) {
		this.addon.logger.debug("Adding priority task with type: {}", task.getType());
		this.priorityTasks.add(task);
	}

	@Override
	public void addImmediateTask(ConfigurationTask task) {
		this.addon.logger.debug("Adding immediate task with type: {}", task.getType());

		if (this.immediateTask != null) {
			throw new RuntimeException(
				"Cannot add an immediate task of type: \"" + task.getType()
				+ "\" when there is already an immediate task of type: \"" + this.immediateTask.getType() + "\"."
			);
		}

		this.immediateTask = task;
	}

	@Override
	public void finishTask(ConfigurationTask.Type type) {
		this.finishCurrentTask(type);
	}

	@Nullable
	@Override
	public ConfigurationTask getCurrentTask() {
		return this.currentTask;
	}
}
