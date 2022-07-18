package org.quiltmc.qsl.component.mixin.level.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.impl.container.LazyComponentContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin implements ComponentProvider {
	private ComponentContainer qsl$container;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void initContainer(RunArgs runArgs, CallbackInfo ci) {
		this.qsl$container = ComponentContainer.builder(this).build(ComponentContainer.LAZY_FACTORY);
	}

	@Override
	public ComponentContainer getComponentContainer() {
		return this.qsl$container;
	}
}
