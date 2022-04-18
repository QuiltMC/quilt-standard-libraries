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

import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.base.api.util.TriState;

import java.util.Objects;
import java.util.function.Predicate;

public class Permissions {

	/**
	 * Gets the {@link TriState state} of a {@code permission} for the given source.
	 *
	 * @param source the source to perform the check for
	 * @param permission the permission to check
	 * @return the state of the permission
	 */
	@NotNull
	public static TriState getPermissionValue(@NotNull ServerCommandSource source, @NotNull Identifier permission) {
		Objects.requireNonNull(source, "source may not be null");
		Objects.requireNonNull(permission, "permission may not be null");

		return PermissionCheckEvent.EVENT.invoker().onPermissionCheck(source, permission);
	}

	/**
	 * Performs a permission check, falling back to the {@code defaultValue} if the resultant
	 * state is {@link TriState#UNSET}.
	 *
	 * @param source the source to perform the check for
	 * @param permission the permission to check
	 * @param defaultValue the default value to use if nothing has been set
	 * @return the result of the permission check
	 */
	public static boolean check(@NotNull ServerCommandSource source, @NotNull Identifier permission, boolean defaultValue) {
		return getPermissionValue(source, permission).toBooleanOrElse(defaultValue);
	}

	/**
	 * Performs a permission check, falling back to requiring the {@code defaultRequiredLevel}
	 * if the resultant state is {@link TriState#UNSET}.
	 *
	 * @param source the source to perform the check for
	 * @param permission the permission to check
	 * @param defaultRequiredLevel the required permission level to check for as a fallback
	 * @return the result of the permission check
	 */
	public static boolean check(@NotNull ServerCommandSource source, @NotNull Identifier permission, int defaultRequiredLevel) {
		return getPermissionValue(source, permission).toBooleanOrElseGet(() -> source.hasPermissionLevel(defaultRequiredLevel));
	}

	/**
	 * Performs a permission check, falling back to {@code false} if the resultant state
	 * is {@link TriState#UNSET}.
	 *
	 * @param source the source to perform the check for
	 * @param permission the permission to check
	 * @return the result of the permission check
	 */
	public static boolean check(@NotNull ServerCommandSource source, @NotNull Identifier permission) {
		return getPermissionValue(source, permission).toBooleanOrElse(false);
	}

	/**
	 * Creates a predicate which returns the result of performing a permission check,
	 * falling back to the {@code defaultValue} if the resultant state is {@link TriState#UNSET}.
	 *
	 * @param permission the permission to check
	 * @param defaultValue the default value to use if nothing has been set
	 * @return a predicate that will perform the permission check
	 */
	@NotNull
	public static Predicate<ServerCommandSource> require(@NotNull Identifier permission, boolean defaultValue) {
		Objects.requireNonNull(permission, "permission may not be null");

		return player -> check(player, permission, defaultValue);
	}

	/**
	 * Creates a predicate which returns the result of performing a permission check,
	 * falling back to requiring the {@code defaultRequiredLevel} if the resultant state is
	 * {@link TriState#UNSET}.
	 *
	 * @param permission the permission to check
	 * @param defaultRequiredLevel the required permission level to check for as a fallback
	 * @return a predicate that will perform the permission check
	 */
	@NotNull
	public static Predicate<ServerCommandSource> require(@NotNull Identifier permission, int defaultRequiredLevel) {
		Objects.requireNonNull(permission, "permission may not be null");

		return player -> check(player, permission, defaultRequiredLevel);
	}

	/**
	 * Creates a predicate which returns the result of performing a permission check,
	 * falling back to {@code false} if the resultant state is {@link TriState#UNSET}.
	 *
	 * @param permission the permission to check
	 * @return a predicate that will perform the permission check
	 */
	@NotNull
	public static Predicate<ServerCommandSource> require(@NotNull Identifier permission) {
		Objects.requireNonNull(permission, "permission may not be null");

		return player -> check(player, permission);
	}

	/**
	 * Gets the {@link TriState state} of a {@code permission} for the given entity.
	 *
	 * @param entity the entity
	 * @param permission the permission
	 * @return the state of the permission
	 */
	@NotNull
	public static TriState getPermissionValue(@NotNull Entity entity, @NotNull Identifier permission) {
		Objects.requireNonNull(entity, "entity may not be null");

		return getPermissionValue(entity.getCommandSource(), permission);
	}

	/**
	 * Performs a permission check, falling back to the {@code defaultValue} if the resultant
	 * state is {@link TriState#UNSET}.
	 *
	 * @param entity the entity to perform the check for
	 * @param permission the permission to check
	 * @param defaultValue the default value to use if nothing has been set
	 * @return the result of the permission check
	 */
	public static boolean check(@NotNull Entity entity, @NotNull Identifier permission, boolean defaultValue) {
		Objects.requireNonNull(entity, "entity may not be null");

		return check(entity.getCommandSource(), permission, defaultValue);
	}

	/**
	 * Performs a permission check, falling back to requiring the {@code defaultRequiredLevel}
	 * if the resultant state is {@link TriState#UNSET}.
	 *
	 * @param entity the entity to perform the check for
	 * @param permission the permission to check
	 * @param defaultRequiredLevel the required permission level to check for as a fallback
	 * @return the result of the permission check
	 */
	public static boolean check(@NotNull Entity entity, @NotNull Identifier permission, int defaultRequiredLevel) {
		Objects.requireNonNull(entity, "entity may not be null");

		return check(entity.getCommandSource(), permission, defaultRequiredLevel);
	}

	/**
	 * Performs a permission check, falling back to {@code false} if the resultant state
	 * is {@link TriState#UNSET}.
	 *
	 * @param entity the entity to perform the check for
	 * @param permission the permission to check
	 * @return the result of the permission check
	 */
	public static boolean check(@NotNull Entity entity, @NotNull Identifier permission) {
		Objects.requireNonNull(entity, "entity may not be null");

		return check(entity.getCommandSource(), permission);
	}

}
