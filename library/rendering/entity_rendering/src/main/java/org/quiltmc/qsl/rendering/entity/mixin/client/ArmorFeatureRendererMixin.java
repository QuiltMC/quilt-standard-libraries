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

import static org.quiltmc.qsl.rendering.entity.impl.client.ArmorRenderingRegistryImpl.LOGGER;

import org.jetbrains.annotations.Nullable;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.model.Model;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EquipmentRenderer;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedRenderState;
import net.minecraft.client.resource.model.EquipmentModelData;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.EquipmentAsset;

import org.quiltmc.qsl.rendering.entity.impl.client.ArmorRenderLayerContext;
import org.quiltmc.qsl.rendering.entity.impl.client.ArmorRenderingRegistryImpl;
import org.quiltmc.qsl.rendering.entity.impl.client.EquipmentRendererExtensions;

@Mixin(ArmorFeatureRenderer.class)
abstract class ArmorFeatureRendererMixin<
		S extends BipedRenderState, M extends BipedEntityModel<S>,
		A extends BipedEntityModel<S>
		> extends FeatureRenderer<S, M> {
	@Shadow
	@Final
	private EquipmentRenderer equipmentRenderer;

	@Unique private S quilt$capturedState;

	private ArmorFeatureRendererMixin(FeatureRendererContext<S, M> context) {
		super(context);
		throw new AssertionError("dummy constructor called");
	}

	@Inject(
			method = "render(Lnet/minecraft/client/util/math/MatrixStack;"
				+ "Lnet/minecraft/client/render/VertexConsumerProvider;I"
				+ "Lnet/minecraft/client/render/entity/state/BipedRenderState;FF)V",
			at = @At("HEAD")
	)
	private void captureState(
			MatrixStack matrices, VertexConsumerProvider vertexConsumers, int i, S state, float f, float g,
			CallbackInfo ci
	) {
		this.quilt$capturedState = state;
	}

	@Inject(method = "getArmor", at = @At("RETURN"), cancellable = true)
	private void quilt$getArmorModel(S state, EquipmentSlot slot, CallbackInfoReturnable<A> cir) {
		@Nullable
		ItemStack equippedStack = switch (slot) {
			case FEET -> state.feetEquipment;
			case LEGS -> state.legsEquipment;
			case CHEST -> state.chestEquipment;
			case HEAD -> state.headEquipment;
			default -> null;
		};

		if (equippedStack != null) {
			A model = cir.getReturnValue();
			BipedEntityModel<BipedRenderState> modifiedModel = ArmorRenderingRegistryImpl
					.getArmorModel((BipedEntityModel<BipedRenderState>) model, state, equippedStack, slot);

			if (modifiedModel != model) {
				// FIXME type safety
				try {
					cir.setReturnValue((A) modifiedModel);
				} catch (ClassCastException e) {
					LOGGER.error("Invalid model", e);
				}
			}
		}
	}

	@WrapOperation(
			method = "renderArmor",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/render/entity/EquipmentRenderer;render("
					+ "Lnet/minecraft/client/resource/model/EquipmentModelData$LayerType;"
					+ "Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;"
					+ "Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;"
					+ "Lnet/minecraft/client/render/VertexConsumerProvider;I)V"
			)
	)
	private void quilt$modifyRenderLayer(
			EquipmentRenderer instance, EquipmentModelData.LayerType layerType, RegistryKey<EquipmentAsset> armorAsset,
			Model model, ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
			Operation<Void> original,
			MatrixStack matrices2, VertexConsumerProvider vertexConsumers2, ItemStack stack2, EquipmentSlot armorSlot
	) {
		EquipmentRendererExtensions extendedEquipmentRenderer =
				(EquipmentRendererExtensions) this.equipmentRenderer;

		try {
			extendedEquipmentRenderer.quilt$setArmorRenderLayerContext(new ArmorRenderLayerContext(
					this.quilt$capturedState, stack, armorSlot, armorAsset
			));

			original.call(instance, layerType, armorAsset, model, stack, matrices, vertexConsumers, light);
		} finally {
			extendedEquipmentRenderer.quilt$clearArmorRenderLayerContext();
		}
	}

	@SuppressWarnings("unchecked")
	@ModifyExpressionValue(
			method = "renderArmor",
			slice = @Slice(from = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/component/type/EquippableComponent;assetKey()Ljava/util/Optional;"
			)),
			at = @At(
				value = "INVOKE", remap = false, ordinal = 0,
				target = "Ljava/util/Optional;orElseThrow()Ljava/lang/Object;"
			)
	)
	private Object quilt$modifyArmorTexture(
			Object assetKey,
			MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack,
			EquipmentSlot slot, int light, A model
	) {
		assetKey = ArmorRenderingRegistryImpl.getArmorAsset(
			(RegistryKey<EquipmentAsset>) assetKey, this.quilt$capturedState,
			stack, slot, stack.hasGlint()
		);

		// TODO trim modification, likely has to go in EquipmentRendererMixin

		return assetKey;
	}

	@Inject(
			method = "render(Lnet/minecraft/client/util/math/MatrixStack;"
				+ "Lnet/minecraft/client/render/VertexConsumerProvider;I"
				+ "Lnet/minecraft/client/render/entity/state/BipedRenderState;FF)V",
			at = @At("RETURN")
	)
	private void quilt$clearCaptures(CallbackInfo ci) {
		this.quilt$capturedState = null;
	}
}
