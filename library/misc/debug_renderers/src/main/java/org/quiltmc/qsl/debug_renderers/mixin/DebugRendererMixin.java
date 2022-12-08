package org.quiltmc.qsl.debug_renderers.mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.quiltmc.qsl.debug_renderers.impl.client.DebugRendererRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {
	@Inject(method = "render", at = @At("TAIL"))
	private void quilt$renderCustomDebugRenderers(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
		DebugRendererRegistry.getEnabledRenderers()
				.forEach(r -> r.render(matrices, vertexConsumers, cameraX, cameraY, cameraZ));
	}
}
