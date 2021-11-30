package org.quiltmc.qsl.key.bindings.mixin.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

@Mixin(KeyBinding.class)
public interface KeyBindingAccessor {
    @Accessor(value = "KEYS_BY_ID")
    static Map<String, KeyBinding> getKeysById() { return null; }

    @Accessor(value = "KEY_TO_BINDINGS")
    static Map<InputUtil.Key, KeyBinding> getKeyToBindings() { return null; }

    @Accessor
    InputUtil.Key getBoundKey();

    @Invoker
    void callReset();
}
