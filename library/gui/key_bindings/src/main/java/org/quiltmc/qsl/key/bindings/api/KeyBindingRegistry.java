package org.quiltmc.qsl.key.bindings.api;

import org.quiltmc.qsl.key.bindings.impl.KeyBindingRegistryImpl;

import net.minecraft.client.option.KeyBinding;

public class KeyBindingRegistry {
    public static KeyBinding registerKeyBinding(KeyBinding key) {
		return registerKeyBinding(key, true);
	}

    public static KeyBinding registerKeyBinding(KeyBinding key, boolean enabled) {
		return KeyBindingRegistryImpl.registerKeyBinding(key, enabled);
	}

	// TODO - Add get method for easier intercompat

    public static void setEnabled(KeyBinding key, boolean newEnabled) {
		KeyBindingRegistryImpl.setEnabled(key, newEnabled);
	}

	// TODO - Add getEnabled method

}
