/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.command.mixin;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.command.api.ServerArgumentType;
import org.quiltmc.qsl.command.impl.ServerArgumentTypes;

@Mixin(CommandManager.class)
public abstract class CommandManagerMixin {
	@Shadow
	@Final
	private CommandDispatcher<ServerCommandSource> dispatcher;

	@Inject(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/brigadier/CommandDispatcher;findAmbiguities(Lcom/mojang/brigadier/AmbiguityConsumer;)V",
					remap = false
			)
	)
	void addQuiltCommands(CommandManager.RegistrationEnvironment environment, CallbackInfo ci) {
		CommandRegistrationCallback.EVENT.invoker().registerCommands(this.dispatcher,
				environmentMatches(environment, CommandManager.RegistrationEnvironment.INTEGRATED),
				environmentMatches(environment, CommandManager.RegistrationEnvironment.DEDICATED)
		);
	}

	@Unique
	private static boolean environmentMatches(CommandManager.RegistrationEnvironment a, CommandManager.RegistrationEnvironment b) {
		return a == b || b == CommandManager.RegistrationEnvironment.ALL;
	}

	// region Copyright 2016, 2017, 2018, 2019, 2020 zml and Colonel contributors
	@SuppressWarnings({"rawtypes", "unchecked"}) // argument type generics
	@Inject(method = "makeTreeForSource", locals = LocalCapture.CAPTURE_FAILEXCEPTION,
			at = @At(value = "INVOKE", target = "com.mojang.brigadier.builder.RequiredArgumentBuilder.getSuggestionsProvider()Lcom/mojang/brigadier/suggestion/SuggestionProvider;", remap = false, ordinal = 0))
	public <T> void replaceArgumentType(CommandNode<ServerCommandSource> tree, CommandNode<CommandSource> result,
	                                    ServerCommandSource source,
	                                    Map<CommandNode<ServerCommandSource>, CommandNode<CommandSource>> nodes,
	                                    CallbackInfo ci, Iterator<?> it,
	                                    CommandNode<ServerCommandSource> current, ArgumentBuilder<?, ?> unused,
	                                    RequiredArgumentBuilder<?, T> builder) throws CommandSyntaxException {
		ServerArgumentType<ArgumentType<T>> type = ServerArgumentTypes.byClass((Class) builder.getType().getClass());
		Set<Identifier> knownExtraCommands = ServerArgumentTypes.getKnownArgumentTypes(source.getPlayer()); // throws an exception, we can ignore bc this is always a player
		// If we have a replacement and the arg type isn't known to the client, change the argument type
		// This is super un-typesafe, but as long as the returned CommandNode is only used for serialization we are fine.
		// Repeat as long as a type is replaceable -- that way you can have a hierarchy of argument types.
		while (type != null && (knownExtraCommands == null || !knownExtraCommands.contains(type.id()))) {
			((RequiredArgumentBuilderAccessor) builder).setType(type.fallbackProvider().createVanillaFallback(builder.getType()));

			if (type.fallbackSuggestions() != null) {
				builder.suggests((SuggestionProvider) type.fallbackSuggestions());
			}

			type = ServerArgumentTypes.byClass((Class) builder.getType().getClass());
		}
	}
	// endregion
}
