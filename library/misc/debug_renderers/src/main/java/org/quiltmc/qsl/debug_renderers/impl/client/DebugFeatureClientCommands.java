package org.quiltmc.qsl.debug_renderers.impl.client;

import static org.quiltmc.qsl.command.api.client.ClientCommandManager.argument;
import static org.quiltmc.qsl.command.api.client.ClientCommandManager.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.command.api.client.ClientCommandRegistrationCallback;
import org.quiltmc.qsl.command.api.client.QuiltClientCommandSource;
import org.quiltmc.qsl.debug_renderers.api.DebugFeature;
import org.quiltmc.qsl.debug_renderers.impl.DebugFeaturesImpl;

@ApiStatus.Internal
@ClientOnly
final class DebugFeatureClientCommands {
	static void init() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) -> dispatcher.register(
				literal("quilt_debug_client").then(
						argument("feature", IdentifierArgumentType.identifier())
								.suggests((c, b) -> CommandSource.suggestIdentifiers(DebugFeaturesImpl.getFeatures().stream().map(DebugFeature::id), b)).then(
										literal("enable").executes(setEnabled(true))
								).then(
										literal("disable").executes(setEnabled(false))
								)
				)
		));
	}

	private static final DynamicCommandExceptionType INVALID_FEATURE = new DynamicCommandExceptionType(id -> Text.literal("No such Debug Feature "+id+"!"));

	private static Command<QuiltClientCommandSource> setEnabled(boolean value) {
		return ctx -> {
			var id = ctx.getArgument("feature", Identifier.class);
			var feature = DebugFeaturesImpl.get(id);
			if (feature == null) {
				throw INVALID_FEATURE.create(id);
			}

			if (feature.needsServer() && !DebugFeaturesImpl.isEnabledOnServer(feature)) {
				var suggestedCommand = "/quilt_debug " + id + " enable";
				ctx.getSource().sendFeedback(
						Text.empty()
								.append(Text.literal("[Debug|Client]: ").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
								.append(Text.literal("Debug Feature " + id + " must be enabled on the server, but it is not - enable it with [")).formatted(Formatting.YELLOW)
								.append(Text.literal(suggestedCommand).styled(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand)).withColor(Formatting.WHITE)))
								.append(Text.literal("]")).formatted(Formatting.YELLOW)
				);
			}

			DebugFeaturesImpl.setEnabledNotifyServer(feature, value);

			ctx.getSource().sendFeedback(
					Text.empty()
							.append(Text.literal("[Debug|Client]: ").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
							.append(Text.literal(id+" "+(value ? "enabled" : "disabled")))
			);
			return Command.SINGLE_SUCCESS;
		};
	}
}
