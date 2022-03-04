package org.quiltmc.qsl.base.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.SharedConstants;

import net.fabricmc.loader.api.FabricLoader;

@Mixin(SharedConstants.class)
public class SharedConstantsMixin {
	@Shadow
	public static boolean isDevelopment = FabricLoader.getInstance().isDevelopmentEnvironment();
}
