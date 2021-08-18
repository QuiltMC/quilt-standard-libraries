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

	static <R> RegistryItemAttribute<R, Boolean> createBool(RegistryKey<Registry<R>> registryKey, Identifier id, boolean defaultValue) {
		return create(registryKey, id, Codec.BOOL, defaultValue);
	}

	static <R> RegistryItemAttribute<R, Integer> createInt(RegistryKey<Registry<R>> registryKey, Identifier id, int defaultValue) {
		return create(registryKey, id, Codec.INT, defaultValue);
	}

	static <R> RegistryItemAttribute<R, Long> createLong(RegistryKey<Registry<R>> registryKey, Identifier id, long defaultValue) {
		return create(registryKey, id, Codec.LONG, defaultValue);
	}

	static <R> RegistryItemAttribute<R, Float> createFloat(RegistryKey<Registry<R>> registryKey, Identifier id, float defaultValue) {
		return create(registryKey, id, Codec.FLOAT, defaultValue);
	}

	static <R> RegistryItemAttribute<R, Double> createDouble(RegistryKey<Registry<R>> registryKey, Identifier id, double defaultValue) {
		return create(registryKey, id, Codec.DOUBLE, defaultValue);
	}

	static <R> RegistryItemAttribute<R, Boolean> createBool(RegistryKey<Registry<R>> registryKey, Identifier id) {
		return create(registryKey, id, Codec.BOOL, null);
	}

	static <R> RegistryItemAttribute<R, Integer> createInt(RegistryKey<Registry<R>> registryKey, Identifier id) {
		return create(registryKey, id, Codec.INT, null);
	}

	static <R> RegistryItemAttribute<R, Long> createLong(RegistryKey<Registry<R>> registryKey, Identifier id) {
		return create(registryKey, id, Codec.LONG, null);
	}

	static <R> RegistryItemAttribute<R, Float> createFloat(RegistryKey<Registry<R>> registryKey, Identifier id) {
		return create(registryKey, id, Codec.FLOAT, null);
	}

	static <R> RegistryItemAttribute<R, Double> createDouble(RegistryKey<Registry<R>> registryKey, Identifier id) {
		return create(registryKey, id, Codec.DOUBLE, null);
	}

	RegistryKey<Registry<R>> getRegistryKey();
	Identifier getId();
	Codec<T> getCodec();
	@Nullable T getDefaultValue();
}
