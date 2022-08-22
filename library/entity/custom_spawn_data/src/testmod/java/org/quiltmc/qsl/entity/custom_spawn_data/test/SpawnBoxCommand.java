package org.quiltmc.qsl.entity.custom_spawn_data.test;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandBuildContext;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SpawnBoxCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandBuildContext buildContext, CommandManager.RegistrationEnvironment environment) {
		dispatcher.register(
				literal("spawnBox")
						.requires(source -> source.hasPermissionLevel(2))
						.then(argument("item", ItemStackArgumentType.itemStack(buildContext))
						.executes(ctx -> {
							ServerWorld world = ctx.getSource().getWorld();
							ItemStack stack = ItemStackArgumentType.getItemStackArgument(ctx, "item")
									.createStack(1, false);
							BoxEntity box = new BoxEntity(stack, world);
							Vec3d pos = ctx.getSource().getPosition();
							box.setPos(pos.x, pos.y + 1, pos.z);
							world.spawnEntity(box);
							ctx.getSource().sendFeedback(Text.literal("Successfully spawned a box on the server."), false);
							return 1;
						}))
		);
	}
}
