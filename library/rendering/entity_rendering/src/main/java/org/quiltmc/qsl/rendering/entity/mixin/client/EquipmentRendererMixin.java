/*
 * Copyright 2025 The Quilt Project
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

package org.quiltmc.qsl.rendering.entity.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EquipmentRenderer;

import org.quiltmc.qsl.rendering.entity.impl.client.ArmorRenderLayerContext;
import org.quiltmc.qsl.rendering.entity.impl.client.ArmorRenderingRegistryImpl;
import org.quiltmc.qsl.rendering.entity.impl.client.EquipmentRendererExtensions;

@Mixin(EquipmentRenderer.class)
abstract class EquipmentRendererMixin implements EquipmentRendererExtensions {
	// 'param' for render method
	@Unique
	private ThreadLocal<ArmorRenderLayerContext> armorRenderLayerContext;

	@Override
	public void quilt$setArmorRenderLayerContext(ArmorRenderLayerContext context) {
		this.armorRenderLayerContext.set(context);
	}

	@Override
	public void quilt$clearArmorRenderLayerContext() {
		this.armorRenderLayerContext.remove();
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void initFields(CallbackInfo ci) {
		this.armorRenderLayerContext = new ThreadLocal<>();
	}

	@ModifyArg(
			method = "render(Lnet/minecraft/client/resource/model/EquipmentModelData$LayerType;"
				+ "Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/item/ItemStack;"
				+ "Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;"
				+ "ILnet/minecraft/util/Identifier;)V",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/render/item/ItemRenderer;getArmorGlintConsumer("
					+ "Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/RenderLayer;Z)"
					+ "Lcom/mojang/blaze3d/vertex/VertexConsumer;"
			)
	)
	private RenderLayer modifyArmorRenderLayer(RenderLayer original) {
		return this.modifyRenderLayerImpl(original);
	}

	@ModifyExpressionValue(
			method = "render(Lnet/minecraft/client/resource/model/EquipmentModelData$LayerType;"
				+ "Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/item/ItemStack;"
				+ "Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I"
				+ "Lnet/minecraft/util/Identifier;)V",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/render/TexturedRenderLayers;getArmorTrim(Z)"
					+ "Lnet/minecraft/client/render/RenderLayer;"
			)
	)
	private RenderLayer modifyTrimRenderLayer(RenderLayer original) {
		return this.modifyRenderLayerImpl(original);
	}

	@Unique
	private RenderLayer modifyRenderLayerImpl(RenderLayer original) {
		final ArmorRenderLayerContext context = this.armorRenderLayerContext.get();
		if (context != null) {
			return ArmorRenderingRegistryImpl.getArmorRenderLayer(
				original,
				context.state(),
				context.stack(),
				context.slot(),
				context.armorAsset()
			);
		} else {
			return original;
		}
	}
}
