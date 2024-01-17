/*
 * Copyright 2023 The Quilt Project
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

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.layout.LinearLayoutWidget;
import net.minecraft.text.Text;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.registry.impl.sync.client.ClientRegistrySync;
import org.quiltmc.qsl.registry.impl.sync.client.LogBuilder;
import org.quiltmc.qsl.registry.impl.sync.client.screen.SyncLogScreen;

@ClientOnly
@Mixin(DisconnectedScreen.class)
public class DisconnectedScreenMixin extends Screen {
	@Shadow
	@Final
	private LinearLayoutWidget grid;

	private List<LogBuilder.Section> quilt$extraLogs;

	protected DisconnectedScreenMixin(Text title) {
		super(title);
	}

	@Inject(method = "<init>*", at = @At("TAIL"))
	private void quilt$storeLogs(Screen parent, Text title, Text reason, CallbackInfo ci) {
		this.quilt$extraLogs = ClientRegistrySync.getAndClearCurrentSyncLogs();
	}

	@Inject(
			method = "init",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/MinecraftClient;isMultiplayerEnabled()Z"
			)
	)
	private void quilt$addLogsButton(CallbackInfo ci) {
		if (!this.quilt$extraLogs.isEmpty()) {
			var logsButton = ButtonWidget.builder(Text.translatableWithFallback("quilt.core.registry_sync.logs_button", "More Details"), (button) -> {
				this.client.setScreen(new SyncLogScreen(this, this.quilt$extraLogs));
			}).build();
			// I might have committed some horrific crimes here
			var settings = this.grid.copyDefaultSettings().setBottomPadding(-5);
			this.grid.add(logsButton, settings);
		}
	}
}
