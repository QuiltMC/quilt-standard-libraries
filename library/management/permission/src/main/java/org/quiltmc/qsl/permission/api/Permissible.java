/*
 * Copyright 2020 lucko (Luck) <luck@lucko.me>
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

package org.quiltmc.qsl.permission.api;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.base.api.util.InjectedInterface;
import org.quiltmc.qsl.base.api.util.TriState;

/**
 * Represents an Entity or CommandSource that can have permissions.
 */
@InjectedInterface({Entity.class, CommandSource.class})
public interface Permissible {

	/**
	 * Gets the {@link TriState state} of a {@code permission} for this source.
	 *
	 * @param permission the permission to check
	 * @return the state of the permission
	 */
	@NotNull
	default TriState getPermission(@NotNull Identifier permission) {
		return TriState.DEFAULT;
	}

	/**
	 * Performs a permission check on this source, falling back to the {@code defaultValue}
	 * if the resultant state is {@link TriState#DEFAULT}.
	 *
	 * @param permission the permission to check
	 * @param defaultValue the default value to use if nothing has been set
	 * @return the result of the permission check
	 */
	default boolean hasPermission(@NotNull Identifier permission, boolean defaultValue) {
		return getPermission(permission).toBooleanOrElse(defaultValue);
	}

	/**
	 * Performs a permission check on this source, falling back to requiring the
	 * {@code defaultRequiredLevel} if the resultant state is {@link TriState#DEFAULT}.
	 *
	 * @param permission the permission to check
	 * @param defaultRequiredLevel the required permission level to check for as a fallback
	 * @return the result of the permission check
	 */
	default boolean hasPermission(@NotNull Identifier permission, int defaultRequiredLevel) {
		return getPermission(permission).toBooleanOrElseGet(() -> ((ServerCommandSource) this).hasPermissionLevel(defaultRequiredLevel));
	}

	/**
	 * Performs a permission check on this source, falling back to {@code false}
	 * if the resultant state is {@link TriState#DEFAULT}.
	 *
	 * @param permission the permission to check
	 * @return the result of the permission check
	 */
	default boolean hasPermission(@NotNull Identifier permission) {
		return getPermission(permission).toBooleanOrElse(false);
	}

}
