package org.quiltmc.qsl.key.bindings.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.quiltmc.qsl.key.bindings.mixin.client.KeyBindingAccessor;

import net.minecraft.client.option.KeyBinding;

public class KeyBindingRegistryImpl {
	private static Map<KeyBinding, Boolean> quiltKeys = new HashMap<>();
	private static List<KeyBindingManager> keyBindingManagers = new ArrayList<>();
	private static KeyBinding[] enabledQuiltKeysArray = new KeyBinding[] {};
	private static KeyBinding[] totalQuiltKeysArray = new KeyBinding[] {};

	public static KeyBinding registerKeyBinding(KeyBinding key, boolean enabled) {
		quiltKeys.put(key, enabled);
		applyChanges(true);
		if (!enabled) {
			KeyBindingAccessor.getKeysById().remove(key.getTranslationKey());
		}
		return key;
	}

	public static void setEnabled(KeyBinding key, boolean newEnabled) {
		if (quiltKeys.containsKey(key)) {
			quiltKeys.replace(key, newEnabled);
			applyChanges(false);
			if (newEnabled) {
				KeyBindingAccessor.getKeysById().put(key.getTranslationKey(), key);
			} else {
				KeyBindingAccessor.getKeysById().remove(key.getTranslationKey(), key);
			}
			((KeyBindingAccessor)key).callReset();
			KeyBinding.updateKeysByCode();
		}
	}

	protected static void registerKeyBindingManager(KeyBindingManager manager) {
		keyBindingManagers.add(manager);
	}

	public static void updateKeysArray(boolean updateTotal) {
		List<KeyBinding> enabledQuiltKeys = new ArrayList<>();
		List<KeyBinding> totalQuiltKeys = new ArrayList<>();
		for (var entry : quiltKeys.entrySet()) {
			if (entry.getValue()) {
				enabledQuiltKeys.add(entry.getKey());
			}

			if (updateTotal) {
				totalQuiltKeys.add(entry.getKey());
			}
		}

		KeyBinding[] quiltKeysArray = enabledQuiltKeys.toArray(new KeyBinding[enabledQuiltKeys.size()]);
		
		enabledQuiltKeysArray = quiltKeysArray;
		if (updateTotal) {
			totalQuiltKeysArray = totalQuiltKeys.toArray(new KeyBinding[totalQuiltKeys.size()]);
		}
	}

	public static KeyBinding[] getKeyBindings(KeyBinding[] allVanillaKeys) {
		return ArrayUtils.addAll(allVanillaKeys, enabledQuiltKeysArray);
	}

	public static KeyBinding[] getAllKeyBindings(KeyBinding[] allVanillaKeys) {
		return ArrayUtils.addAll(allVanillaKeys, totalQuiltKeysArray);
	}

	public static void applyChanges(boolean updateTotal) {
		updateKeysArray(updateTotal);
		for (KeyBindingManager manager : keyBindingManagers) {
			manager.addModdedKeyBinds();
		}
	}
}
