/*
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

package org.quiltmc.qsl.permission.mixin;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.base.api.util.TriState;
import org.quiltmc.qsl.permission.api.Permissible;
import org.quiltmc.qsl.permission.api.Permissions;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Objects;

@Mixin(ServerCommandSource.class)
public class ServerCommandSourceMixin implements Permissible {

	@NotNull
	@Override
	public TriState getPermissionValue(@NotNull Identifier permission) {
		Objects.requireNonNull(permission, "permission may not be null");

		return Permissions.getPermissionValue((ServerCommandSource) (Object) this, permission);
	}

}
