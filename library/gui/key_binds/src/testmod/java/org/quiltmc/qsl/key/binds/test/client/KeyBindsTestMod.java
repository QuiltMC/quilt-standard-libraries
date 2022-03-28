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
import net.minecraft.util.Hand;

import org.quiltmc.qsl.key.binds.api.KeyBindRegistry;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;

@Environment(EnvType.CLIENT)
public class KeyBindsTestMod implements ClientTickEvents.Start {
	public static final String KEY_CATEGORY = "key.qsl.category";

	// A conflicting key test
	public static final KeyBind CONFLICT_TEST_KEY = KeyBindRegistry.registerKeyBind(
		new KeyBind("key.qsl.conflict_test", GLFW.GLFW_KEY_H, KEY_CATEGORY)
	);

	public static final KeyBind DISABLE_KEY_BIND_KEY = KeyBindRegistry.registerKeyBind(
		new KeyBind("key.qsl.disable_key_bind", GLFW.GLFW_KEY_H, KEY_CATEGORY), true
	);

	public static final KeyBind ENABLE_KEY_BIND_KEY = KeyBindRegistry.registerKeyBind(
		new KeyBind("key.qsl.enable_key_bind", GLFW.GLFW_KEY_I, KEY_CATEGORY), true
	);

	public static final KeyBind DISABLED_CONFLICT_TEST_KEY = KeyBindRegistry.registerKeyBind(
		new KeyBind("key.qsl.disabled_conflict_test", GLFW.GLFW_KEY_H, KEY_CATEGORY), false
	);

	public static final KeyBind CLAP_KEY = KeyBindRegistry.registerKeyBind(
		new KeyBind("key.qsl.clap", InputUtil.UNKNOWN_KEY.getKeyCode(), KEY_CATEGORY).withChord(
			InputUtil.Type.MOUSE.createFromKeyCode(GLFW.GLFW_MOUSE_BUTTON_LEFT),
			InputUtil.Type.MOUSE.createFromKeyCode(GLFW.GLFW_MOUSE_BUTTON_RIGHT)
		)
	);

	@Override
	public void startClientTick(MinecraftClient client) {
		if (client.player == null) return;

		if (DISABLE_KEY_BIND_KEY.isPressed()) {
			client.player.sendMessage(new LiteralText("The key has disappeared! Bye bye, key!"), true);
			KeyBindRegistry.setEnabled(DISABLE_KEY_BIND_KEY, false);
		}

		if (ENABLE_KEY_BIND_KEY.isPressed()) {
			client.player.sendMessage(new LiteralText("The key is back!"), true);
			KeyBindRegistry.setEnabled(DISABLE_KEY_BIND_KEY, true);
		}

		if (CONFLICT_TEST_KEY.isPressed()) {
			client.player.sendMessage(new LiteralText("This is the conflict key being pressed"), false);
		}

		if (DISABLED_CONFLICT_TEST_KEY.isPressed()) {
			client.player.sendMessage(new LiteralText("I'm not supposed to do things! Why am I enabled?"), false);
		}

		if (CLAP_KEY.wasPressed()) {
			client.player.sendMessage(new LiteralText("*clap*"), true);
			client.player.playSound(SoundEvents.ENTITY_GENERIC_SMALL_FALL, 1.5F, 1.5F);
		}
	}
}
