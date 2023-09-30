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

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import org.quiltmc.qsl.item.group.api.client.ItemGroupIconRenderer;
import org.quiltmc.qsl.item.group.impl.CreativeGuiExtensions;
import org.quiltmc.qsl.item.group.impl.ItemGroupIconRendererRegistry;
import org.quiltmc.qsl.item.group.impl.QuiltCreativePlayerInventoryScreenWidgets;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeScreenHandler> implements CreativeGuiExtensions {
	private final Map<Integer, Integer> PAGE_TO_SELECTED_INDEX = new Object2ObjectOpenHashMap<>();

	private CreativeInventoryScreenMixin(CreativeInventoryScreen.CreativeScreenHandler screenHandler, PlayerInventory playerInventory, Text title) {
		super(screenHandler, playerInventory, title);
	}

	@Shadow
	protected abstract void setSelectedTab(ItemGroup itemGroup);

	@Shadow
	public abstract int getSelectedTab();

	/**
	 * In order to match the behavior of Vanilla where closing and opening the creative inventory brings you to the previously open item group, we also replicate this behavior with the current page
	 */
	@Unique
	private static int quilt$currentPage = 0;

	@Unique
	private int getPageOffset(int page) {
		if (page == 0) {
			return 0;
		}

		return 12 + ((12 - QuiltCreativePlayerInventoryScreenWidgets.ALWAYS_SHOWN_GROUPS.size()) * (page - 1));
	}

	@Unique
	private int getOffsetPage(int offset) {
		if (offset < 12) {
			return 0;
		} else {
			return 1 + ((offset - 12) / (12 - QuiltCreativePlayerInventoryScreenWidgets.ALWAYS_SHOWN_GROUPS.size()));
		}
	}

	@Override
	public void quilt$nextPage() {
		if (this.getPageOffset(quilt$currentPage + 1) >= ItemGroup.GROUPS.length) {
			return;
		}

		this.PAGE_TO_SELECTED_INDEX.compute(quilt$currentPage, (page, pos) -> this.getSelectedTab());

		quilt$currentPage++;
		this.quilt$updateSelection();
	}

	@Override
	public void quilt$previousPage() {
		if (quilt$currentPage == 0) {
			return;
		}

		this.PAGE_TO_SELECTED_INDEX.compute(quilt$currentPage, (page, pos) -> this.getSelectedTab());

		quilt$currentPage--;
		this.quilt$updateSelection();
	}

	@Override
	public boolean quilt$isButtonVisible(QuiltCreativePlayerInventoryScreenWidgets.Type type) {
		return ItemGroup.GROUPS.length > 12;
	}

	@Override
	public boolean quilt$isButtonEnabled(QuiltCreativePlayerInventoryScreenWidgets.Type type) {
		if (type == QuiltCreativePlayerInventoryScreenWidgets.Type.NEXT) {
			return !(this.getPageOffset(quilt$currentPage + 1) >= ItemGroup.GROUPS.length);
		}

		if (type == QuiltCreativePlayerInventoryScreenWidgets.Type.PREVIOUS) {
			return quilt$currentPage != 0;
		}

		return false;
	}

	@Unique
	private void quilt$updateSelection() {
		int pageMaxIndex = this.getPageOffset(quilt$currentPage);
		int pageMinIndex = this.getPageOffset(quilt$currentPage + 1) - 1;
		int selectedTab = this.getSelectedTab();

		if (selectedTab < pageMaxIndex || selectedTab > pageMinIndex) {
			this.setSelectedTab(ItemGroup.GROUPS[this.PAGE_TO_SELECTED_INDEX.getOrDefault(quilt$currentPage, this.getPageOffset(quilt$currentPage))]);
		}
	}

	@Inject(method = "init", at = @At("RETURN"))
	private void init(CallbackInfo info) {
		this.quilt$updateSelection();

		int xpos = x + 116;
		int ypos = y - 10;

		this.addDrawableChild(new QuiltCreativePlayerInventoryScreenWidgets.ItemGroupButtonWidget(xpos + 11, ypos, QuiltCreativePlayerInventoryScreenWidgets.Type.NEXT, this));
		this.addDrawableChild(new QuiltCreativePlayerInventoryScreenWidgets.ItemGroupButtonWidget(xpos, ypos, QuiltCreativePlayerInventoryScreenWidgets.Type.PREVIOUS, this));
	}

	@Inject(method = "setSelectedTab", at = @At("HEAD"), cancellable = true)
	private void setSelectedTab(ItemGroup itemGroup, CallbackInfo info) {
		if (this.quilt$isGroupNotVisible(itemGroup)) {
			info.cancel();
		}
	}

	@Inject(method = "renderTabTooltipIfHovered", at = @At("HEAD"), cancellable = true)
	private void renderTabTooltipIfHovered(MatrixStack matrixStack, ItemGroup itemGroup, int mx, int my, CallbackInfoReturnable<Boolean> info) {
		if (this.quilt$isGroupNotVisible(itemGroup)) {
			info.setReturnValue(false);
		}
	}

	@Inject(method = "isClickInTab", at = @At("HEAD"), cancellable = true)
	private void isClickInTab(ItemGroup itemGroup, double mx, double my, CallbackInfoReturnable<Boolean> info) {
		if (this.quilt$isGroupNotVisible(itemGroup)) {
			info.setReturnValue(false);
		}
	}

	@Inject(method = "renderTabIcon", at = @At("HEAD"), cancellable = true)
	private void renderTabIcon(MatrixStack matrixStack, ItemGroup itemGroup, CallbackInfo info) {
		if (this.quilt$isGroupNotVisible(itemGroup)) {
			info.cancel();
		}
	}

	@ModifyArgs(method = "renderTabIcon", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderInGuiWithOverrides(Lnet/minecraft/item/ItemStack;II)V"))
	private void renderCustomTabIcon(Args args, MatrixStack matrixStack, ItemGroup itemGroup) {
		ItemGroupIconRenderer<ItemGroup> iconRenderer = ItemGroupIconRendererRegistry.get(itemGroup);
		if (iconRenderer != null) {
			args.set(0, ItemStack.EMPTY); // itemStack = ItemStack.EMPTY;
			// args.get(1) x
			// args.get(2) y
			iconRenderer.render(itemGroup, matrixStack, args.get(1), args.get(2), MinecraftClient.getInstance().getTickDelta());
		}
	}

	@Unique
	private boolean quilt$isGroupNotVisible(ItemGroup itemGroup) {
		if (QuiltCreativePlayerInventoryScreenWidgets.ALWAYS_SHOWN_GROUPS.contains(itemGroup)) {
			return false;
		}

		return quilt$currentPage != this.getOffsetPage(itemGroup.getIndex());
	}

	@Override
	public int quilt$currentPage() {
		return quilt$currentPage;
	}
}
