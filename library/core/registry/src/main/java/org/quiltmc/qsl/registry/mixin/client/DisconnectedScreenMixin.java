/*
 * Copyright 2023 QuiltMC
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

package org.quiltmc.qsl.registry.mixin.client;

import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.registry.impl.sync.client.ClientRegistrySync;
import org.quiltmc.qsl.registry.impl.sync.client.LogBuilder;
import org.quiltmc.qsl.registry.impl.sync.client.screen.SyncLogScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@ClientOnly
@Mixin(DisconnectedScreen.class)
public class DisconnectedScreenMixin extends Screen {
	@Shadow
	private int reasonHeight;
	private List<LogBuilder.Section> quilt$extraLogs;

	protected DisconnectedScreenMixin(Text title) {
		super(title);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void quilt$storeLogs(Screen parent, Text title, Text reason, CallbackInfo ci) {
		this.quilt$extraLogs = ClientRegistrySync.getAndClearCurrentSyncLogs();
	}

	@ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;positionAndSize(IIII)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;"), index = 1)
	private int quilt$addLogsButton(int height) {
		return this.quilt$extraLogs.isEmpty() ? height : height + 25;
	}


	@Inject(method = "init", at = @At("TAIL"))
	private void quilt$addLogsButton(CallbackInfo ci) {
		if (!this.quilt$extraLogs.isEmpty()) {
			ButtonWidget.Builder var1 = ButtonWidget.builder(Text.translatable("quilt.core.registry_sync.logs_button", "More Details"), (button) -> {
				this.client.setScreen(new SyncLogScreen(this, this.quilt$extraLogs));
			});
			int x = this.width / 2 - 100;
			int y = this.height / 2 + this.reasonHeight / 2;
			Objects.requireNonNull(this.textRenderer);
			this.addDrawableChild(var1.positionAndSize(x, Math.min(y + 9, this.height - 30), 200, 20).build());
		}
	}
}
