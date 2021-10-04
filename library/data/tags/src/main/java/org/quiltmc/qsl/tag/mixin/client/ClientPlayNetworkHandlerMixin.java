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

package org.quiltmc.qsl.tag.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeTagsS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.registry.DynamicRegistryManager;

import org.quiltmc.qsl.tag.impl.TagRegistryImpl;
import org.quiltmc.qsl.tag.impl.client.ClientTagRegistryManager;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
	@Inject(
			method = "onGameJoin",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/packet/s2c/play/GameJoinS2CPacket;getRegistryManager()Lnet/minecraft/util/registry/DynamicRegistryManager;",
					shift = At.Shift.AFTER
			)
	)
	private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
		ClientTagRegistryManager.applyAll(packet.getRegistryManager());
	}

	@Inject(method = "onDisconnected", at = @At("TAIL"))
	private void onDisconnected(Text reason, CallbackInfo ci) {
		// @TODO Replace with networking API?
		ClientTagRegistryManager.applyAll(DynamicRegistryManager.create());
	}

	@Inject(
			method = "onSynchronizeTags",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/tag/RequiredTagListRegistry;getMissingTags(Lnet/minecraft/tag/TagManager;)Lcom/google/common/collect/Multimap;"
			)
	)
	private void onGetMissingTagsStart(SynchronizeTagsS2CPacket packet, CallbackInfo ci) {
		TagRegistryImpl.startClientMissingTagsFetching();
	}

	@Inject(
			method = "onSynchronizeTags",
			at = @At(
					value = "INVOKE",
					target = "Lcom/google/common/collect/Multimap;isEmpty()Z"
			)
	)
	private void onGetMissingTagsEnd(SynchronizeTagsS2CPacket packet, CallbackInfo ci) {
		TagRegistryImpl.endClientMissingTagsFetching();
	}
}
