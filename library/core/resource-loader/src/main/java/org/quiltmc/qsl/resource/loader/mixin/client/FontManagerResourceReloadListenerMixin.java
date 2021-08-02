package org.quiltmc.qsl.resource.loader.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;
import org.quiltmc.qsl.resource.loader.api.reloader.ResourceReloaderKeys;

@Mixin(targets = "net/minecraft/client/font/FontManager$1")
public abstract class FontManagerResourceReloadListenerMixin implements IdentifiableResourceReloader {
	@Override
	public Identifier getQuiltId() {
		return ResourceReloaderKeys.Client.FONTS;
	}
}
