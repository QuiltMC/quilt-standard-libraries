/*
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

package org.quiltmc.qsl.item.content_registry.mixin;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import org.quiltmc.qsl.item.content_registry.api.ItemContentRegistries;
import org.quiltmc.qsl.registry.attachment.impl.RegistryEntryAttachmentHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.ComposterBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.Holder;
import net.minecraft.util.registry.Registry;

@Mixin(ComposterBlock.class)
public abstract class ComposterBlockMixin {


	@Redirect(method = "compost", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2FloatMap;containsKey(Ljava/lang/Object;)Z"))
	private static boolean useAttachmentStatic(Object2FloatMap<ItemConvertible> instance, Object o) {
		return ItemContentRegistries.COMPOST_CHANCE.getValue((Item) o).isPresent();
	}

	@Redirect(method = "onUse", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2FloatMap;containsKey(Ljava/lang/Object;)Z"))
	private boolean useAttachment(Object2FloatMap<ItemConvertible> instance, Object o) {
		return ItemContentRegistries.COMPOST_CHANCE.getValue((Item) o).isPresent();
	}

	@Redirect(method = "addToComposter", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2FloatMap;getFloat(Ljava/lang/Object;)F"))
	private static float setChance(Object2FloatMap<ItemConvertible> instance, Object o) {
		return ItemContentRegistries.COMPOST_CHANCE.getValue((Item) o).orElse(-1f);
	}

	@Inject(method = "registerDefaultCompostableItems", at = @At("TAIL"))
	private static void addAttachmentEntries(CallbackInfo ci) {
		var builtin = RegistryEntryAttachmentHolder.getBuiltin(Registry.ITEM);
		builtin.valueTable.row(ItemContentRegistries.COMPOST_CHANCE).forEach((item, o) -> registerCompostableItem((float) o, item));
		builtin.valueTagTable.row(ItemContentRegistries.COMPOST_CHANCE).forEach((tag, o) -> {
			for (Holder<Item> holder : Registry.ITEM.getTagOrEmpty(tag)) {
				registerCompostableItem((float) o, holder.value());
			}
		});
	}

	@Inject(method = "registerCompostableItem", at = @At("HEAD"))
	private static void addToAttachment(float levelIncreaseChance, ItemConvertible item, CallbackInfo ci) {
		ItemContentRegistries.COMPOST_CHANCE.put(item.asItem(), levelIncreaseChance);
	}

	@Shadow
	private static void registerCompostableItem(float levelIncreaseChance, ItemConvertible item) {
	}
}