package org.quiltmc.qsl.commands.test;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.LiteralText;
import org.quiltmc.qsl.commands.api.CommandRegistrationCallback;
import org.quiltmc.qsl.commands.api.client.ClientCommandManager;
import org.quiltmc.qsl.commands.api.client.ClientCommandRegistrationCallback;

import static net.minecraft.server.command.CommandManager.literal;

public class CommandsApiTest implements ModInitializer {
	@Override
	public void onInitialize() {
			CommandRegistrationCallback.EVENT.register((dispatcher, integrated, dedicated) -> {
			if (dedicated) {
				dispatcher.register(literal("ping")
						.executes(ctx -> {
							ctx.getSource().sendFeedback(new LiteralText("pong!"), false);
							return 0;
						})
				);
			} else if (integrated) {
				dispatcher.register(literal("singleplayer_only")
						.executes(ctx -> {
							ctx.getSource().sendFeedback(new LiteralText("This command should only exist in singleplayer"), false);
							return 0;
						})
				);
			}

			dispatcher.register(literal("quilt")
					.executes(ctx -> {
						//noinspection OptionalGetWithoutIsPresent
						ctx.getSource().sendFeedback(new LiteralText("Quilt Version: "+FabricLoader.getInstance().getModContainer("quilt_base").get().getMetadata().getVersion().getFriendlyString()), false);
						return 0;
					})
			);
		});

		ClientCommandRegistrationCallback.EVENT.register(dispatcher -> dispatcher.register(
				ClientCommandManager.literal("test_client_command")
						.executes(ctx -> {
							ctx.getSource().sendFeedback(new LiteralText("It works!"));
							return 0;
						})
		));
	}
}
