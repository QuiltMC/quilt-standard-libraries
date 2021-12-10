package org.quiltmc.qsl.key.bindings.test.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.option.KeyBinding;

import org.quiltmc.qsl.key.bindings.api.KeyBindingRegistry;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;

@Environment(EnvType.CLIENT)
public class KeyBindingRegistryTestMod implements ClientModInitializer {
	public static final Logger LOGGER = LogManager.getFormatterLogger("KeyBindingRegistryTest");

	@Override
	public void onInitializeClient() {
		ClientLifecycleEvents.READY.register(lifecycleClient -> {
			KeyBinding enableKeyBindKey = KeyBindingRegistry.getKeyBinding("key.qsl.enable_key_bind");

			if (enableKeyBindKey != null) {
				LOGGER.info("Successfully got the \"Enable Key Bind\" key!");

				ClientTickEvents.END.register(tickClient -> {
					if (enableKeyBindKey.wasPressed()) {
						LOGGER.info("I can add behavior to other keys!");
					}
				});
			}

			LOGGER.info("The registry has the following keys registered:");
			KeyBindingRegistry.getAllKeyBindings(true).forEach((key, value) -> {
				LOGGER.info("%s: %s", key.getTranslationKey(), value);
			});
		});
	}
}
