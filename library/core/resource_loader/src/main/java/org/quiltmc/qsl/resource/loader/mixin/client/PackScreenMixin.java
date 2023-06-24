/*
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

package org.quiltmc.qsl.resource.loader.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.client.gui.screen.pack.PackListWidget.ResourcePackEntry;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.text.Text;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.resource.loader.impl.QuiltBuiltinResourcePackProfile.BuiltinResourcePackSource;

@ClientOnly
@Mixin(PackScreen.class)
public abstract class PackScreenMixin extends Screen {
	@Shadow
	private PackListWidget availablePackList;

	@Shadow
	private PackListWidget selectedPackList;

	private PackScreenMixin(Text text) {
		super(text);
	}

	@SuppressWarnings("unchecked")
	@Inject(method = "render", at = @At("TAIL"))
	private void renderTooltips(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		ResourcePackEntry availableEntry = this.availablePackList.getHoveredEntry();
		if (availableEntry != null) {
			if (((ResourcePackEntryAccessor) availableEntry).getPack().getSource() instanceof BuiltinResourcePackSource source) {
				graphics.drawTooltip(this.textRenderer, source.getTooltip(), mouseX, mouseY);
			}
		}

		ResourcePackEntry selectedEntry = this.selectedPackList.getHoveredEntry();
		if (selectedEntry != null) {
			if (((ResourcePackEntryAccessor) selectedEntry).getPack().getSource() instanceof BuiltinResourcePackSource source) {
				graphics.drawTooltip(this.textRenderer, source.getTooltip(), mouseX, mouseY);
			}
		}
	}
}
