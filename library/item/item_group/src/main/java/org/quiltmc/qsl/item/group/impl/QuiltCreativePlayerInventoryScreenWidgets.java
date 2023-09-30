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

package org.quiltmc.qsl.item.group.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@ApiStatus.Internal
public final class QuiltCreativePlayerInventoryScreenWidgets {
	private static final Identifier BUTTON_TEXTURE = new Identifier("quilt_item_group", "textures/gui/creative_buttons.png");
	public static final Set<ItemGroup> ALWAYS_SHOWN_GROUPS = new HashSet<>();

	static {
		ALWAYS_SHOWN_GROUPS.add(ItemGroup.SEARCH);
		ALWAYS_SHOWN_GROUPS.add(ItemGroup.INVENTORY);
		ALWAYS_SHOWN_GROUPS.add(ItemGroup.HOTBAR);
	}

	public static class ItemGroupButtonWidget extends ButtonWidget {
		public static final String TRANSLATION_KEY = "quilt_item_group.gui.creative_tab_page";
		private final CreativeGuiExtensions extensions;
		private final CreativeInventoryScreen gui;
		private final Type type;

		public ItemGroupButtonWidget(int x, int y, Type type, CreativeGuiExtensions extensions) {
			super(x, y, 11, 10, type.text, (bw) -> type.clickConsumer.accept(extensions));
			this.extensions = extensions;
			this.type = type;
			this.gui = (CreativeInventoryScreen) extensions;
		}

		@Override
		public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			this.visible = this.extensions.quilt$isButtonVisible(this.type);
			this.active = this.extensions.quilt$isButtonEnabled(this.type);

			if (this.visible) {
				int u = this.active && this.hovered ? 22 : 0;
				int v = this.active ? 0 : 10;

				RenderSystem.setShaderTexture(0, BUTTON_TEXTURE);
				RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
				this.drawTexture(matrices, this.x, this.y, u + (this.type == Type.NEXT ? 11 : 0), v, 11, 10);

				if (this.hovered) {
					this.gui.renderTooltip(matrices, Text.translatable(TRANSLATION_KEY, this.extensions.quilt$currentPage() + 1, ((ItemGroup.GROUPS.length - 12) / 9) + 2), mouseX, mouseY);
				}
			}
		}
	}

	public enum Type {
		NEXT(Text.of(">"), CreativeGuiExtensions::quilt$nextPage),
		PREVIOUS(Text.of("<"), CreativeGuiExtensions::quilt$previousPage);

		final Text text;
		final Consumer<CreativeGuiExtensions> clickConsumer;

		Type(Text text, Consumer<CreativeGuiExtensions> clickConsumer) {
			this.text = text;
			this.clickConsumer = clickConsumer;
		}
	}
}
