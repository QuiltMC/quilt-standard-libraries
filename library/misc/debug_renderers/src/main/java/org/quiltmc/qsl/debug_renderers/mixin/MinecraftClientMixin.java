package org.quiltmc.qsl.debug_renderers.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.quiltmc.qsl.debug_renderers.api.client.DebugRendererRegistrationCallback;
import org.quiltmc.qsl.debug_renderers.impl.client.DebugRendererRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Inject(method = "<init>", at = @At(value = "INVOKE", shift = At.Shift.BY, by=2, target = "Lnet/minecraft/client/render/debug/DebugRenderer;<init>(Lnet/minecraft/client/MinecraftClient;)V"))
	private void quilt$initDebugRenderers(RunArgs args, CallbackInfo ci) {
		DebugRendererRegistrationCallback.EVENT.invoker().registerDebugRenderers(DebugRendererRegistry::register);
	}
}
