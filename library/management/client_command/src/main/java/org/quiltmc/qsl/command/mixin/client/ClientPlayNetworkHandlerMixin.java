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

package org.quiltmc.qsl.command.mixin.client;

import com.mojang.brigadier.CommandDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.registry.ClientRegistryLayer;
import net.minecraft.command.CommandBuildContext;
import net.minecraft.command.CommandSource;
import net.minecraft.feature_flags.FeatureFlagBitSet;
import net.minecraft.network.packet.s2c.play.CommandTreeUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.registry.LayeredRegistryManager;
import net.minecraft.server.command.CommandManager;

import org.quiltmc.qsl.command.impl.client.ClientCommandInternals;

@Mixin(ClientPlayNetworkHandler.class)
abstract class ClientPlayNetworkHandlerMixin {
	@Shadow
	private CommandDispatcher<CommandSource> commandDispatcher;

	@Shadow
	@Final
	private ClientCommandSource commandSource;

	@Shadow
	private LayeredRegistryManager<ClientRegistryLayer> clientRegistryManager;

	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	private FeatureFlagBitSet enabledFlags;

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Inject(method = "onGameJoin", at = @At("RETURN"))
	private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
		ClientCommandInternals.updateCommands(CommandBuildContext.createConfigurable(this.clientRegistryManager.getCompositeManager(), this.enabledFlags),
				(CommandDispatcher) this.commandDispatcher, this.commandSource,
				this.client.isIntegratedServerRunning() ? CommandManager.RegistrationEnvironment.INTEGRATED
						: CommandManager.RegistrationEnvironment.DEDICATED
		);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Inject(method = "onCommandTreeUpdate", at = @At("RETURN"))
	private void onOnCommandTree(CommandTreeUpdateS2CPacket packet, CallbackInfo info) {
		ClientCommandInternals.updateCommands(null,
				(CommandDispatcher) this.commandDispatcher, this.commandSource,
				this.client.isIntegratedServerRunning() ? CommandManager.RegistrationEnvironment.INTEGRATED
						: CommandManager.RegistrationEnvironment.DEDICATED
		);
	}

	@Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
	private void onSendCommand(String command, CallbackInfoReturnable<Boolean> cir) {
		if (ClientCommandInternals.executeCommand(command, true)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "sendChatCommand", at = @At("HEAD"), cancellable = true)
	private void onSendCommand(String command, CallbackInfo ci) {
		if (ClientCommandInternals.executeCommand(command, true)) {
			ci.cancel();
		}
	}
}
