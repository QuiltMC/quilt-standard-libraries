package org.quiltmc.qsl.key.bindings.mixin.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {
    @Mutable
    @Final
    private static Map<String, Integer> CATEGORY_ORDER_MAP;

    @Inject(
        at = @At("TAIL"),
        method = "<init>(Ljava/lang/String;Lnet/minecraft/client/util/InputUtil$Type;ILjava/lang/String;)V"
    )
    private void addModdedCategory(String string, InputUtil.Type type, int i, String string2, CallbackInfo ci) {
        if (!CATEGORY_ORDER_MAP.containsKey(string2)) {
            CATEGORY_ORDER_MAP.put(string2, CATEGORY_ORDER_MAP.size() + 1);
        }
    }
}
