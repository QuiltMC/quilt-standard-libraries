/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2022-2023 QuiltMC
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

import static org.quiltmc.qsl.rendering.entity.impl.client.ArmorRenderingRegistryImpl.LOGGER;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.rendering.entity.api.client.ArmorTextureUtils;
import org.quiltmc.qsl.rendering.entity.impl.client.ArmorRenderingRegistryImpl;

@SuppressWarnings("rawtypes")
@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorFeatureRendererMixin {
	@Shadow
	@Final
	private static Map<String, Identifier> ARMOR_TEXTURE_CACHE;

	@Unique
	private LivingEntity quilt$capturedEntity;
	@Unique
	private EquipmentSlot quilt$capturedSlot;

	@Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
			at = @At("HEAD"))
	private void quilt$captureEntity(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, LivingEntity livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
		this.quilt$capturedEntity = livingEntity;
	}

	@Inject(method = "renderArmor", at = @At("HEAD"))
	private void quilt$captureSlot(MatrixStack matrices, VertexConsumerProvider vertexConsumers, LivingEntity livingEntity, EquipmentSlot slot, int i, BipedEntityModel bipedEntityModel, CallbackInfo ci) {
		this.quilt$capturedSlot = slot;
	}

	@Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
			at = @At("RETURN"))
	private void quilt$uncapture(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, LivingEntity livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
		this.quilt$capturedEntity = null;
		this.quilt$capturedSlot = null;
	}

	@Inject(method = "getArmor", at = @At("RETURN"), cancellable = true)
	private void quilt$getArmorModel(EquipmentSlot slot, CallbackInfoReturnable<BipedEntityModel<LivingEntity>> cir) {
		ItemStack stack = this.quilt$capturedEntity.getEquippedStack(slot);

		BipedEntityModel<LivingEntity> model = cir.getReturnValue();
		model = ArmorRenderingRegistryImpl.getArmorModel(model, this.quilt$capturedEntity, stack, this.quilt$capturedSlot);
		cir.setReturnValue(model);
	}

	@Inject(method = "getArmorTexture",
			at = @At(value = "INVOKE", target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;"),
			cancellable = true)
	private void quilt$getArmorTexture(ArmorItem armorItem, boolean useSecondTexture, String suffix, CallbackInfoReturnable<Identifier> cir) {
		ItemStack stack = this.quilt$capturedEntity.getEquippedStack(this.quilt$capturedSlot);

		Identifier texture = ARMOR_TEXTURE_CACHE.computeIfAbsent(
				armorItem.getMaterial().getTexture()
						+ ArmorTextureUtils.getArmorTextureSuffix(useSecondTexture, suffix)
						+ ".png",
				Identifier::new);
		texture = ArmorRenderingRegistryImpl.getArmorTexture(texture, this.quilt$capturedEntity, stack, this.quilt$capturedSlot,
				useSecondTexture, suffix);

		if (!texture.getPath().contains(".")) {
			LOGGER.warn("Armor texture identifier '" + texture + "' is missing file extension, automatically appending '.png'");
			texture = new Identifier(texture.getNamespace(), texture.getPath() + ".png");
		}

		cir.setReturnValue(ARMOR_TEXTURE_CACHE.computeIfAbsent(texture.toString(), Identifier::new));
	}
}
