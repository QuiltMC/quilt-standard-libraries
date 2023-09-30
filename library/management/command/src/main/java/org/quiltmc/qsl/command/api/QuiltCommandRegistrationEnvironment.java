/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.command.api;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.server.command.CommandManager;

import org.quiltmc.qsl.base.api.util.InjectedInterface;

/**
 * Represents an extension to the {@link CommandManager.RegistrationEnvironment} enum,
 * and is automatically injected into it.
 */
@ApiStatus.NonExtendable
@InjectedInterface(CommandManager.RegistrationEnvironment.class)
public interface QuiltCommandRegistrationEnvironment {
	/**
	 * {@return {@code true} if the environment corresponds to the dedicated server, otherwise {@code false}}
	 */
	boolean isDedicated();

	/**
	 * {@return {@code true} if the environment corresponds to single-player, otherwise {@code false}}
	 */
	boolean isIntegrated();
}
