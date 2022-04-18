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
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.util.TriState;

import java.util.UUID;

/**
 * Simple permissions check event for {@link CommandSource}s.
 */
public interface PermissionCheckEvent {

	Event<PermissionCheckEvent> EVENT = Event.create(PermissionCheckEvent.class, (callbacks) -> (source, permission) -> {
		for (PermissionCheckEvent callback : callbacks) {
			TriState state = callback.onPermissionCheck(source, permission);
			if (state != TriState.UNSET) {
				return state;
			}
		}
		return TriState.UNSET;
	});

	/**
	 * This method is called whenever a mod performs a permission check on a {@link ServerCommandSource}.
	 *
	 * Note: When this method isn't overridden, it will call {@link #onPermissionCheck(UUID, Identifier)}.
	 *       The UUID that is passed in will be either the UUID of the CommandSource's entity or, if it
	 *       doesn't have one, {@link Util#NIL_UUID}.
	 *
	 * @param source the ServerCommandSource to check the permission for
	 * @param permission the permission to check for
	 * @return a {@link TriState} value representing the outcome of the permission check.
	 */
	@NotNull
	default TriState onPermissionCheck(@NotNull ServerCommandSource source, @NotNull Identifier permission) {
		Entity sourceEntity = source.getEntity();
		return onPermissionCheck(sourceEntity == null ? Util.NIL_UUID : sourceEntity.getUuid(), permission);
	}

	/**
	 * This method is called whenever a mod performs a permission check on an entity with the specified {@link UUID}.
	 *
	 * @param source the UUID to check the permission for
	 * @param permission the permission to check for
	 * @return a {@link TriState} value representing the outcome of the permission check.
	 */
	@NotNull
	TriState onPermissionCheck(@NotNull UUID source, @NotNull Identifier permission);

}
