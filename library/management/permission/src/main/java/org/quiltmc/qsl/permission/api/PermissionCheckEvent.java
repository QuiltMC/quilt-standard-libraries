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

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.EventAwareListener;
import org.quiltmc.qsl.base.api.util.TriState;

/**
 * Simple permissions check event for {@link ServerCommandSource}s.
 */
public interface PermissionCheckEvent extends EventAwareListener {

	Event<PermissionCheckEvent> EVENT = Event.create(PermissionCheckEvent.class, (callbacks) -> (source, permission) -> {
		for (PermissionCheckEvent callback : callbacks) {
			TriState state = callback.onPermissionCheck(source, permission);
			if (state != TriState.DEFAULT) {
				return state;
			}
		}
		return TriState.DEFAULT;
	});

	/**
	 * This method is called whenever a mod performs a permission check on a {@link ServerCommandSource}.
	 *
	 * @param source the ServerCommandSource to check the permission for
	 * @param permission the permission to check for
	 * @return a {@link TriState} value representing the outcome of the permission check.
	 */
	@NotNull
	TriState onPermissionCheck(@NotNull ServerCommandSource source, @NotNull Identifier permission);

}
