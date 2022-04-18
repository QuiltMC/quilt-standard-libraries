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

package org.quiltmc.qsl.permission;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.base.api.util.TriState;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.permission.api.PermissionCheckEvent;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.command.argument.IdentifierArgumentType.getIdentifier;
import static net.minecraft.command.argument.IdentifierArgumentType.identifier;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.quiltmc.qsl.permission.api.Permissions.require;

public class QuiltPermissionTest implements ServerLifecycleEvents.Starting, CommandRegistrationCallback {

	private static List<Identifier> permissions;

	@Override
	public void startingServer(MinecraftServer server) {
		permissions = new ArrayList<>();
		PermissionCheckEvent.EVENT.register((source, permission) -> TriState.fromBoolean(permissions.contains(permission)));
	}

	@Override
	public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, boolean integrated, boolean dedicated) {
		dispatcher.register(literal("checkpermission").then(argument("permission", identifier()).executes(ctx -> {
			Identifier permission = getIdentifier(ctx, "permission");
			ctx.getSource().sendFeedback(Text.of("Permission '" + permission + "' is set to " + ctx.getSource().checkPermission(permission) + "."), false);

			return 0;
		})));

		dispatcher.register(literal("permissionrequired").requires(require(new Identifier("quilt_permission", "required_permission"))).executes(ctx -> {
			ctx.getSource().sendFeedback(Text.of("Woo, you have the required permission!"), false);
			return 0;
		}));

		dispatcher.register(literal("togglepermission").then(argument("permission", identifier()).executes(ctx -> {
			Identifier permission = getIdentifier(ctx, "permission");
			if (permissions.contains(permission)) {
				permissions.remove(permission);
				ctx.getSource().sendFeedback(Text.of("Permission '" + permission + "' has been set to false."), true);
			} else {
				permissions.add(permission);
				ctx.getSource().sendFeedback(Text.of("Permission '" + permission + "' has been set to true."), true);
			}

			return 0;
		})));
	}

}
