package org.quiltmc.qsl.key.bindings.impl;

import org.quiltmc.qsl.key.bindings.mixin.client.GameOptionsAccessor;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;

public class KeyBindingManager {
    private final GameOptions options;
    private final KeyBinding[] allKeys;

    public KeyBindingManager(GameOptions options, KeyBinding[] allKeys) {
        this.options = options;
        this.allKeys = allKeys;
        this.addModdedKeyBinds();
        KeyBindingRegistryImpl.registerKeyBindingManager(this);
    }

    public void addModdedKeyBinds() {
		((GameOptionsAccessor)(Object)this.options).setKeysAll(KeyBindingRegistryImpl.getKeyBindings(this.allKeys));
	}
}
