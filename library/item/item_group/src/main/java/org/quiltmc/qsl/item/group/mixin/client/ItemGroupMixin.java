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

package org.quiltmc.qsl.item.group.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.ItemGroup;

import org.quiltmc.qsl.item.group.impl.QuiltCreativePlayerInventoryScreenWidgets;

@Mixin(ItemGroup.class)
public abstract class ItemGroupMixin {
	@Shadow
	public abstract int getIndex();

	@Shadow
	public abstract boolean isTopRow();

	@Inject(method = "isTopRow", cancellable = true, at = @At("HEAD"))
	private void isTopRow(CallbackInfoReturnable<Boolean> info) {
		if (this.getIndex() > 11) {
			info.setReturnValue((this.getIndex() - 12) % (12 - QuiltCreativePlayerInventoryScreenWidgets.ALWAYS_SHOWN_GROUPS.size()) < 4);
		}
	}

	@Inject(method = "getColumn", cancellable = true, at = @At("HEAD"))
	private void getColumn(CallbackInfoReturnable<Integer> info) {
		if (this.getIndex() > 11) {
			if (this.isTopRow()) {
				info.setReturnValue((this.getIndex() - 12) % (12 - QuiltCreativePlayerInventoryScreenWidgets.ALWAYS_SHOWN_GROUPS.size()));
			} else {
				info.setReturnValue((this.getIndex() - 12) % (12 - QuiltCreativePlayerInventoryScreenWidgets.ALWAYS_SHOWN_GROUPS.size()) - 4);
			}
		}
	}
}
