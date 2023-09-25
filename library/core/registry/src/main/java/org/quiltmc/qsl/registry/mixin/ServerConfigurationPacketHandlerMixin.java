package org.quiltmc.qsl.registry.mixin;

import java.util.Queue;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.registry.impl.sync.server.QuiltSyncTask;
import org.quiltmc.qsl.registry.impl.sync.server.QuiltSyncTaskHolder;
import org.quiltmc.qsl.registry.impl.sync.server.ServerRegistrySync;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.ServerConfigurationPacketHandler;
import net.minecraft.network.configuration.ConfigurationTask;

@Mixin(ServerConfigurationPacketHandler.class)
public abstract class ServerConfigurationPacketHandlerMixin implements AbstractServerPacketHandlerAccessor, QuiltSyncTaskHolder {
	@Shadow
	@Final
	private Queue<ConfigurationTask> tasks;

	@Shadow
	@Nullable
	private ConfigurationTask currentTask;

	@Shadow
	protected abstract void finishCurrentTask(ConfigurationTask.Type taskType);

	@Inject(method = "addOptionalTasks", at = @At("TAIL"))
	private void quilt$addSyncTask(CallbackInfo ci) {
		if (ServerRegistrySync.shouldSync()) {
			this.tasks.add(new QuiltSyncTask((ServerConfigurationPacketHandler) (Object) this, this.getConnection()));
		}
	}

	@Override
	public @Nullable QuiltSyncTask qsl$getSyncTask() {
        if (this.currentTask instanceof QuiltSyncTask task) return task;
        throw new IllegalStateException("Not currently in QuiltSyncTask!");
    }

	@Override
	public void qsl$finishSyncTask() {
		this.finishCurrentTask(QuiltSyncTask.TYPE);
	}
}
