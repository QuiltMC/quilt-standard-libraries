/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.itemgroup.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.Text;

import org.quiltmc.qsl.itemgroup.impl.CreativeGuiExtensions;
import org.quiltmc.qsl.itemgroup.impl.QuiltCreativeGuiComponents;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativePlayerInventoryGuiMixin extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeScreenHandler> implements CreativeGuiExtensions {
	public CreativePlayerInventoryGuiMixin(CreativeInventoryScreen.CreativeScreenHandler screenHandler, PlayerInventory playerInventory, Text title) {
		super(screenHandler, playerInventory, title);
	}

	@Shadow
	protected abstract void setSelectedTab(ItemGroup itemGroup_1);

	@Shadow
	public abstract int getSelectedTab(); /* XXX getSelectedTab XXX */

	// "static" matches selectedTab
	@Unique
	private static int qsl$currentPage = 0;

	@Unique
	private int getPageOffset(int page) {
		return switch (page) {
			case 0 -> 0;
			case 1 -> 12;
			default -> 12 + ((12 - QuiltCreativeGuiComponents.ALWAYS_SHOWN_GROUPS.size()) * (page - 1));
		};
	}

	@Unique
	private int getOffsetPage(int offset) {
		if (offset < 12) {
			return 0;
		} else {
			return 1 + ((offset - 12) / (12 - QuiltCreativeGuiComponents.ALWAYS_SHOWN_GROUPS.size()));
		}
	}

	@Override
	public void qsl$nextPage() {
		if (getPageOffset(qsl$currentPage + 1) >= ItemGroup.GROUPS.length) {
			return;
		}

		qsl$currentPage++;
		qsl$updateSelection();
	}

	@Override
	public void qsl$previousPage() {
		if (qsl$currentPage == 0) {
			return;
		}

		qsl$currentPage--;
		qsl$updateSelection();
	}

	@Override
	public boolean qsl$isButtonVisible(QuiltCreativeGuiComponents.Type type) {
		return ItemGroup.GROUPS.length > 12;
	}

	@Override
	public boolean qsl$isButtonEnabled(QuiltCreativeGuiComponents.Type type) {
		if (type == QuiltCreativeGuiComponents.Type.NEXT) {
			return !(getPageOffset(qsl$currentPage + 1) >= ItemGroup.GROUPS.length);
		}

		if (type == QuiltCreativeGuiComponents.Type.PREVIOUS) {
			return qsl$currentPage != 0;
		}

		return false;
	}

	@Unique
	private void qsl$updateSelection() {
		int minPos = getPageOffset(qsl$currentPage);
		int maxPos = getPageOffset(qsl$currentPage + 1) - 1;
		int curPos = getSelectedTab();

		if (curPos < minPos || curPos > maxPos) {
			setSelectedTab(ItemGroup.GROUPS[getPageOffset(qsl$currentPage)]);
		}
	}

	@Inject(method = "init", at = @At("RETURN"))
	private void init(CallbackInfo info) {
		qsl$updateSelection();

		int xpos = x + 116;
		int ypos = y - 10;

		addDrawableChild(new QuiltCreativeGuiComponents.ItemGroupButtonWidget(xpos + 11, ypos, QuiltCreativeGuiComponents.Type.NEXT, this));
		addDrawableChild(new QuiltCreativeGuiComponents.ItemGroupButtonWidget(xpos, ypos, QuiltCreativeGuiComponents.Type.PREVIOUS, this));
	}

	@Inject(method = "setSelectedTab", at = @At("HEAD"), cancellable = true)
	private void setSelectedTab(ItemGroup itemGroup, CallbackInfo info) {
		if (qsl$isGroupNotVisible(itemGroup)) {
			info.cancel();
		}
	}

	@Inject(method = "renderTabTooltipIfHovered", at = @At("HEAD"), cancellable = true)
	private void renderTabTooltipIfHovered(MatrixStack matrixStack, ItemGroup itemGroup, int mx, int my, CallbackInfoReturnable<Boolean> info) {
		if (qsl$isGroupNotVisible(itemGroup)) {
			info.setReturnValue(false);
		}
	}

	@Inject(method = "isClickInTab", at = @At("HEAD"), cancellable = true)
	private void isClickInTab(ItemGroup itemGroup, double mx, double my, CallbackInfoReturnable<Boolean> info) {
		if (qsl$isGroupNotVisible(itemGroup)) {
			info.setReturnValue(false);
		}
	}

	@Inject(method = "renderTabIcon", at = @At("HEAD"), cancellable = true)
	private void renderTabIcon(MatrixStack matrixStack, ItemGroup itemGroup, CallbackInfo info) {
		if (qsl$isGroupNotVisible(itemGroup)) {
			info.cancel();
		}
	}

	@Unique
	private boolean qsl$isGroupNotVisible(ItemGroup itemGroup) {
		if (QuiltCreativeGuiComponents.ALWAYS_SHOWN_GROUPS.contains(itemGroup)) {
			return false;
		}

		return qsl$currentPage != getOffsetPage(itemGroup.getIndex());
	}

	@Override
	public int qsl$currentPage() {
		return qsl$currentPage;
	}
}
