/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.rendering.entity.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrimPermutation;
import net.minecraft.registry.Holder;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.rendering.entity.impl.client.ArmorRenderingRegistryImpl;

@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorFeatureRendererMixin {
	@Unique
	private LivingEntity quilt$capturedEntity;
	@Unique
	private EquipmentSlot quilt$capturedSlot;
	@Unique
	private Identifier quilt$capturedArmorTexture;

	@Inject(
			method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
			at = @At("HEAD")
	)
	private void quilt$captureEntity(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, LivingEntity livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
		this.quilt$capturedEntity = livingEntity;
	}

	@Inject(method = "renderArmor(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;ILnet/minecraft/client/render/entity/model/BipedEntityModel;)V", at = @At("HEAD"))
	private void quilt$captureSlot(MatrixStack matrices, VertexConsumerProvider vertexConsumers, LivingEntity livingEntity, EquipmentSlot slot, int i, BipedEntityModel<?> bipedEntityModel, CallbackInfo ci) {
		this.quilt$capturedSlot = slot;
	}

	@Inject(method = "getArmor", at = @At("RETURN"), cancellable = true)
	private void quilt$getArmorModel(EquipmentSlot slot, CallbackInfoReturnable<BipedEntityModel<LivingEntity>> cir) {
		ItemStack stack = this.quilt$capturedEntity.getEquippedStack(slot);

		BipedEntityModel<LivingEntity> model = cir.getReturnValue();
		model = ArmorRenderingRegistryImpl.getArmorModel(model, this.quilt$capturedEntity, stack, slot);
		cir.setReturnValue(model);
	}

	@WrapOperation(
			method = "renderArmor(Lnet/minecraft/registry/Holder;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/item/trim/ArmorTrimPermutation;Lnet/minecraft/client/render/entity/model/BipedEntityModel;Z)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/texture/SpriteAtlasTexture;getSprite(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/texture/Sprite;"
			)
	)
	private Sprite quilt$getArmorTexture(SpriteAtlasTexture instance, Identifier texture, Operation<Sprite> original, Holder<ArmorMaterial> material, MatrixStack matrices, VertexConsumerProvider verticies, int i, ArmorTrimPermutation trimPermutation, BipedEntityModel<?> model, boolean hasGlint) {
		ItemStack stack = this.quilt$capturedEntity.getEquippedStack(this.quilt$capturedSlot);
		texture = ArmorRenderingRegistryImpl.getArmorTexture(texture, this.quilt$capturedEntity, stack, this.quilt$capturedSlot, hasGlint);

		return original.call(instance, texture);
	}

	@ModifyArg(
			method = "renderArmorParts",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/VertexConsumerProvider;getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
			),
			index = 0
	)
	private RenderLayer quilt$modifyArmorRenderLayer(RenderLayer layer) {
		return ArmorRenderingRegistryImpl.getArmorRenderLayer(layer,
				this.quilt$capturedEntity,
				this.quilt$capturedEntity.getEquippedStack(this.quilt$capturedSlot),
				this.quilt$capturedSlot,
				this.quilt$capturedArmorTexture);
	}

	@ModifyArg(
			method = "renderArmor(Lnet/minecraft/registry/Holder;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/item/trim/ArmorTrimPermutation;Lnet/minecraft/client/render/entity/model/BipedEntityModel;Z)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/texture/SpriteAtlasTexture;getSprite(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/texture/Sprite;"
			),
			index = 0
	)
	private Identifier quilt$modifyArmorTrimTexture(Identifier texture) {
		// TODO
		return this.quilt$capturedArmorTexture = texture;
	}

	@ModifyArg(
			method = "renderArmor(Lnet/minecraft/registry/Holder;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/item/trim/ArmorTrimPermutation;Lnet/minecraft/client/render/entity/model/BipedEntityModel;Z)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/VertexConsumerProvider;getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
			),
			index = 0
	)
	private RenderLayer quilt$modifyArmorTrimRenderLayer(RenderLayer layer) {
		return ArmorRenderingRegistryImpl.getArmorRenderLayer(layer,
				this.quilt$capturedEntity,
				this.quilt$capturedEntity.getEquippedStack(this.quilt$capturedSlot),
				this.quilt$capturedSlot,
				this.quilt$capturedArmorTexture);
	}

	@Inject(
			method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
			at = @At("RETURN")
	)
	private void quilt$uncapture(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, LivingEntity livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
		this.quilt$capturedEntity = null;
		this.quilt$capturedSlot = null;
		this.quilt$capturedArmorTexture = null;
	}
}
