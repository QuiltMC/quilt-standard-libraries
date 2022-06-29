package org.quiltmc.qsl.component.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.component.api.ComponentContainer;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.impl.container.LazifiedComponentContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient implements ComponentProvider {
	private ComponentContainer qsl$container;

	@Override
	public @NotNull ComponentContainer getContainer() {
		return this.qsl$container;
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void initContainer(RunArgs runArgs, CallbackInfo ci) {
		this.qsl$container = LazifiedComponentContainer.builder(this).orElseThrow().build();
	}
}
