/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.game_rule.mixin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.GameRules;

import org.quiltmc.qsl.game_rule.impl.EnumRuleCommand;
import org.quiltmc.qsl.game_rule.impl.EnumRuleType;

@Mixin(targets = "net/minecraft/server/command/GameRuleCommand$C_pmgkjwet") // Anonymous GameRule.Visitor in GameRuleCommand#register
public abstract class GameRuleCommandVisitorMixin {
	@Dynamic("literalArgumentBuilder in GameRuleCommand#register")
	@Shadow
	private LiteralArgumentBuilder<ServerCommandSource> field_19419;

	@Inject(at = @At("HEAD"), method = "method_20762(Lnet/minecraft/world/GameRules$Key;Lnet/minecraft/world/GameRules$Type;)V", cancellable = true)
	private <T extends GameRules.Rule<T>> void onRegisterCommand(GameRules.Key<T> key, GameRules.Type<T> type, CallbackInfo ci) {
		// Check if our type is a EnumRuleType
		if (type instanceof EnumRuleType) {
			//noinspection rawtypes,unchecked
			EnumRuleCommand.register(this.field_19419, (GameRules.Key) key, (EnumRuleType) type);
			ci.cancel();
		}
	}
}
