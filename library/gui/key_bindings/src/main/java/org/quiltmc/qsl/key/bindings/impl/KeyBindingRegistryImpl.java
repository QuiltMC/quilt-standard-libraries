package org.quiltmc.qsl.key.bindings.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.quiltmc.qsl.key.bindings.mixin.client.GameOptionsAccessor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

public class KeyBindingRegistryImpl {
	private static Map<KeyBinding, Boolean> quiltKeys = new HashMap<>();
	private static KeyBinding[] allVanillaKeys = new KeyBinding[] {};

	public static KeyBinding registerKeyBinding(KeyBinding key, boolean enabled) {
		quiltKeys.put(key, enabled);
		applyChanges();
		return key;
	}

	public static void setActive(KeyBinding key, boolean newEnabled) {
		quiltKeys.replace(key, newEnabled);
		applyChanges();
	}

	public static void setVanillaKeys(KeyBinding[] allKeys) {
		allVanillaKeys = allKeys;
	}

	public static KeyBinding[] updateKeysArray() {
		List<KeyBinding> enabledQuiltKeys = new ArrayList<>();
		for (var entry : quiltKeys.entrySet()) {
			if (entry.getValue()) {
				enabledQuiltKeys.add(entry.getKey());
			}
		}

		KeyBinding[] quiltKeysArray = enabledQuiltKeys.toArray(new KeyBinding[enabledQuiltKeys.size()]);
		
		return ArrayUtils.addAll(allVanillaKeys, quiltKeysArray);
	}

	public static void applyChanges() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.options != null) {
			((GameOptionsAccessor)(Object)client.options).setKeysAll(updateKeysArray());
		}
	}
}
