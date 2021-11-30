package org.quiltmc.qsl.key.bindings.mixin.client;

import java.io.File;

import org.quiltmc.qsl.key.bindings.impl.KeyBindingRegistryImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;

@Mixin(GameOptions.class)
public class GameOptionsMixin {
	// While there is an injection on the constructor, this Mutable is necessary
	@Mutable
	@Shadow
	@Final
	public KeyBinding[] keysAll;

	@Inject(
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/option/GameOptions;load()V"
			),
			method = "<init>"
	)
	private void modifyAllKeys(MinecraftClient client, File file, CallbackInfo ci) {
		KeyBindingRegistryImpl.setVanillaKeys(this.keysAll.clone());
		this.keysAll = KeyBindingRegistryImpl.updateKeysArray();
	}
}
