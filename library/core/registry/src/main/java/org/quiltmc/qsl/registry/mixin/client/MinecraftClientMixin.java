package org.quiltmc.qsl.registry.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.registry.impl.sync.ClientRegistrySync;
import org.quiltmc.qsl.registry.impl.sync.SynchronizedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("TAIL"))
	private void polymer_onDisconnect(CallbackInfo ci) {
		for (var reg : Registry.REGISTRIES) {
			if (reg instanceof SynchronizedRegistry registry && registry.quilt$requiresSyncing()) {
				System.out.println("Restored snapshot of " + ((Registry<Registry<?>>) Registry.REGISTRIES).getId(reg));
				registry.quilt$restoreIdSnapshot();
			}
		}
		ClientRegistrySync.rebuildStates();
	}
}
