package org.quiltmc.qsl.rendering.entity.impl.client;

import java.util.HashMap;
import java.util.Map;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import net.minecraft.item.ArmorMaterial;
import net.minecraft.util.Identifier;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class FallbackArmorTextureProvider {
	private FallbackArmorTextureProvider() {
		throw new UnsupportedOperationException("FallbackArmorTextureProvider only contains static declarations.");
	}

	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<ArmorMaterial, Identifier> CACHE = new HashMap<>();

	public static @NotNull Identifier getArmorTexture(@NotNull ArmorMaterial material) {
		return CACHE.computeIfAbsent(material, FallbackArmorTextureProvider::createArmorTexture);
	}

	private static @NotNull Identifier createArmorTexture(@NotNull ArmorMaterial material) {
		LOGGER.warn(material.getName() + " (" + material + ") did not implement getTexture()! Using fallback implementation");
		return new Identifier("textures/model/armor/" + material.getName());
	}
}
