package org.quiltmc.qsl.registry.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.registry.impl.sync.SynchronizedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public class ConnectScreenMixin {
	@Inject(method = "connect(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;)V", at = @At("HEAD"))
	private void quilt$snapshotRegistry(MinecraftClient client, ServerAddress address, CallbackInfo ci) {
		for (var reg : Registry.REGISTRIES) {
			if (reg instanceof SynchronizedRegistry registry && registry.quilt$requiresSyncing()) {
				System.out.println("Created snapshot of " + ((Registry<Registry<?>>) Registry.REGISTRIES).getId(reg));
				registry.quilt$createIdSnapshot();
			}
		}
	}
}
