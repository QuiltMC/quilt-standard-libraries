package org.quiltmc.qsl.registry.attribute.api;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.qsl.registry.attribute.impl.RegistryItemAttributeImpl;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

public interface RegistryItemAttribute<R, T> {
	static <R, T> RegistryItemAttribute<R, T> create(RegistryKey<Registry<R>> registryKey, Identifier id, Codec<T> codec,
													 @Nullable T defaultValue) {
		return RegistryItemAttributeImpl.create(registryKey, id, codec, defaultValue);
	}

	static <R, T> RegistryItemAttribute<R, T> create(RegistryKey<Registry<R>> registryKey, Identifier id, Codec<T> codec) {
		return create(registryKey, id, codec, null);
	}

	RegistryKey<Registry<R>> getRegistryKey();
	Identifier getId();
	Codec<T> getCodec();
	@Nullable T getDefaultValue();
}
