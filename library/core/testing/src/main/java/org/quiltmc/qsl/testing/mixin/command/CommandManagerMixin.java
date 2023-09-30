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

package org.quiltmc.qsl.testing.mixin.command;

import com.mojang.brigadier.CommandDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.SharedConstants;
import net.minecraft.command.CommandBuildContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.dev.TestCommand;

import org.quiltmc.qsl.testing.impl.game.QuiltGameTestImpl;

@Mixin(CommandManager.class)
public abstract class CommandManagerMixin {
	@Shadow
	@Final
	private CommandDispatcher<ServerCommandSource> dispatcher;

	@Inject(
			method = "<init>",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/WorldBorderCommand;register(Lcom/mojang/brigadier/CommandDispatcher;)V", shift = At.Shift.AFTER)
	)
	private void construct(CommandManager.RegistrationEnvironment environment, CommandBuildContext context, CallbackInfo info) {
		// Registered by vanilla when isDevelopment is enabled.
		if (!SharedConstants.isDevelopment && QuiltGameTestImpl.COMMAND_ENABLED) {
			TestCommand.register(this.dispatcher);
		}
	}
}
