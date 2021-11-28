package org.quiltmc.qsl.key.bindings.impl;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.option.KeyBinding;

public class KeyBindingRegistryImpl {
	private static KeyBinding[] allVanillaKeys = new KeyBinding[] {};
	private static KeyBinding[] keysArray = new KeyBinding[] {
		new KeyBinding("key.hello_world", GLFW.GLFW_KEY_H, "key.categories.misc")
	};

	public static void setVanillaKeys(KeyBinding[] allKeys) {
		allVanillaKeys = allKeys;
	}

	public static KeyBinding[] getKeysArray() {
		return ArrayUtils.addAll(allVanillaKeys, keysArray);
	}
}
