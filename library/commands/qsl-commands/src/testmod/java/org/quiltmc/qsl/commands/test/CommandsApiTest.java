package org.quiltmc.qsl.commands.test;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.LiteralText;
import org.quiltmc.qsl.commands.api.CommandRegistrationCallback;

public class CommandsApiTest implements ModInitializer {
	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registerForIntegrated, registerForDedicated) -> {
			if (registerForDedicated) {
				dispatcher.register(CommandManager.literal("ping")
						.executes(ctx -> {
							ctx.getSource().sendFeedback(new LiteralText("pong!"), false);
							return 0;
						})
				);
			}

			if (registerForIntegrated) {
				dispatcher.register(CommandManager.literal("singleplayer_only")
						.executes(ctx -> {
							ctx.getSource().sendFeedback(new LiteralText("This command should only exist in singleplayer"), false);
							return 0;
						})
				);
			}

			dispatcher.register(CommandManager.literal("quilt")
					.executes(ctx -> {
						ctx.getSource().sendFeedback(new LiteralText("Quilt Version: "+FabricLoader.getInstance().getModContainer("quilt_base").get().getMetadata().getVersion().getFriendlyString()), false);
						return 0;
					})
			);
		});
	}
}
