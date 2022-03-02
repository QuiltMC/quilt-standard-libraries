package org.quiltmc.qsl.key.binds.mixin.client.config;

import org.quiltmc.qsl.key.binds.impl.config.QuiltKeyBindsConfigManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.option.KeyBind;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    // You can't INVOKE_ASSIGN at GameOptions for some reason
    @Inject(
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/TutorialManager;<init>(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/option/GameOptions;)V"),
        method = "<init>"
    )
    private void handleQuiltKeyBindsConfig(RunArgs runArgs, CallbackInfo ci) {
        QuiltKeyBindsConfigManager.loadConfig();
        KeyBind.updateBoundKeys();
    }
}
