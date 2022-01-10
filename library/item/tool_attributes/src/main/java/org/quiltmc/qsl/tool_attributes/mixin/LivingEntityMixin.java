/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.tool_attributes.mixin;

import com.google.common.collect.Multimap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.tool_attributes.impl.ItemStackContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Nullable
	@Unique private ItemStack stackContext = null;
	@Nullable
	@Unique private EquipmentSlot slotContext = null;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/AttributeContainer;removeModifiers(Lcom/google/common/collect/Multimap;)V"), method = "getEquipment", locals = LocalCapture.CAPTURE_FAILHARD)
	private void storeRemoveStackContext(CallbackInfoReturnable<Map> cir, Map map, EquipmentSlot[] var2, int var3, int var4, EquipmentSlot equipmentSlot, ItemStack oldStack, ItemStack newStack) {
		stackContext = oldStack;
		slotContext = equipmentSlot;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/AttributeContainer;removeModifiers(Lcom/google/common/collect/Multimap;)V"), method = "getEquipment")
	private void setupRemoveModifierContext(AttributeContainer attributeContainer, Multimap<EntityAttribute, EntityAttributeModifier> oldModifiers) {
		((ItemStackContext) (Object) stackContext).quiltToolAttributes_setContext((LivingEntity) (Object) this);
		Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers = stackContext.getAttributeModifiers(slotContext);
		((ItemStackContext) (Object) stackContext).quiltToolAttributes_setContext(null);
		attributeContainer.removeModifiers(attributeModifiers);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/AttributeContainer;addTemporaryModifiers(Lcom/google/common/collect/Multimap;)V"), method = "getEquipment", locals = LocalCapture.CAPTURE_FAILHARD)
	private void storeAddStackContext(CallbackInfoReturnable<Map> cir, Map map, EquipmentSlot[] var2, int var3, int var4, EquipmentSlot equipmentSlot, ItemStack oldStack, ItemStack newStack) {
		stackContext = newStack;
		slotContext = equipmentSlot;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/AttributeContainer;addTemporaryModifiers(Lcom/google/common/collect/Multimap;)V"), method = "getEquipment")
	private void setupAddModifierContext(AttributeContainer attributeContainer, Multimap<EntityAttribute, EntityAttributeModifier> oldModifiers) {
		((ItemStackContext) (Object) stackContext).quiltToolAttributes_setContext((LivingEntity) (Object) this);
		Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers = stackContext.getAttributeModifiers(slotContext);
		((ItemStackContext) (Object) stackContext).quiltToolAttributes_setContext(null);
		attributeContainer.addTemporaryModifiers(attributeModifiers);
	}
}
