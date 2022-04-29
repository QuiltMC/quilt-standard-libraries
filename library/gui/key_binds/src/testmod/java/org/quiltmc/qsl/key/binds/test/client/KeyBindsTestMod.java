/*
 * Copyright 2021-2022 QuiltMC
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

package org.quiltmc.qsl.key.binds.test.client;

import com.mojang.blaze3d.platform.InputUtil;
import org.lwjgl.glfw.GLFW;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBind;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.key.binds.api.KeyBindRegistry;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;

@Environment(EnvType.CLIENT)
public class KeyBindsTestMod implements ClientModInitializer, ClientTickEvents.Start {
	public static final String KEY_CATEGORY = "key.qsl.category";

	// A conflicting key test
	public static final KeyBind CONFLICT_TEST_KEY = new KeyBind("key.qsl.conflict_test", GLFW.GLFW_KEY_H, KEY_CATEGORY);

	public static final KeyBind DISABLE_KEY_BIND_KEY = new KeyBind("key.qsl.disable_key_bind", GLFW.GLFW_KEY_H, KEY_CATEGORY);

	public static final KeyBind ENABLE_KEY_BIND_KEY = new KeyBind("key.qsl.enable_key_bind", GLFW.GLFW_KEY_I, KEY_CATEGORY);

	public static final KeyBind DISABLED_CONFLICT_TEST_KEY = new KeyBind("key.qsl.disabled_conflict_test", GLFW.GLFW_KEY_H, KEY_CATEGORY);

	public static final KeyBind CLAP_KEY = new KeyBind("key.qsl.clap", InputUtil.UNKNOWN_KEY.getKeyCode(), KEY_CATEGORY).withChord(
		InputUtil.Type.MOUSE.createFromKeyCode(GLFW.GLFW_MOUSE_BUTTON_LEFT),
		InputUtil.Type.MOUSE.createFromKeyCode(GLFW.GLFW_MOUSE_BUTTON_RIGHT)
	);

	@Override
	public void onInitializeClient(ModContainer mod) {
		DISABLED_CONFLICT_TEST_KEY.disable();

		KeyBindRegistry.registerKeyBinds(
				CONFLICT_TEST_KEY,
				DISABLE_KEY_BIND_KEY,
				ENABLE_KEY_BIND_KEY,
				DISABLED_CONFLICT_TEST_KEY,
				CLAP_KEY
		);
	}

	// TODO - Add a test for toggling vanilla key binds; They probably don't work right now, but actually seeing that in action would help a lot
	@Override
	public void startClientTick(MinecraftClient client) {
		if (client.player == null) return;

		if (DISABLE_KEY_BIND_KEY.wasPressed()) {
			client.player.sendMessage(new LiteralText("The clap key has disappeared!"), true);
			CLAP_KEY.disable();
			client.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.5F, 1.5F);
		}

		if (ENABLE_KEY_BIND_KEY.wasPressed()) {
			client.player.sendMessage(new LiteralText("The clap key is back!"), true);
			CLAP_KEY.enable();
			client.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.5F, 1.5F);
		}

		if (CONFLICT_TEST_KEY.wasPressed()) {
			client.player.sendMessage(new LiteralText("This is the conflict key being pressed"), false);
		}

		if (DISABLED_CONFLICT_TEST_KEY.wasPressed()) {
			client.player.sendMessage(new LiteralText("I'm not supposed to do things! Why am I enabled?"), false);
		}

		if (CLAP_KEY.wasPressed()) {
			client.player.sendMessage(new LiteralText("*clap*"), true);
			client.player.playSound(SoundEvents.ENTITY_GENERIC_SMALL_FALL, 1.5F, 1.5F);
		}
	}
}
