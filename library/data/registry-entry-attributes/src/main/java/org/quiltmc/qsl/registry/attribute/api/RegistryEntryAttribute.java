package org.quiltmc.qsl.registry.attribute.api;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.qsl.registry.attribute.impl.RegistryEntryAttributeImpl;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import java.util.Map;

public interface RegistryEntryAttribute<R, T> {
	static <R, T> RegistryEntryAttribute<R, T> create(RegistryKey<Registry<R>> registryKey, Identifier id, Codec<T> codec,
													  @Nullable T defaultValue) {
		return RegistryEntryAttributeImpl.create(registryKey, id, codec, defaultValue);
	}

	static <R, T> RegistryEntryAttribute<R, T> create(RegistryKey<Registry<R>> registryKey, Identifier id, Codec<T> codec) {
		return create(registryKey, id, codec, null);
	}

	static <R, T extends DispatchedType> RegistryEntryAttribute<R, T> createDispatched(RegistryKey<Registry<R>> registryKey, Identifier id,
																					   Map<String, Codec<? extends T>> codecs, @Nullable T defaultValue) {
		return create(registryKey, id, Codec.STRING.dispatch(T::getType, codecs::get), defaultValue);
	}

	static <R, T extends DispatchedType> RegistryEntryAttribute<R, T> createDispatched(RegistryKey<Registry<R>> registryKey, Identifier id,
																					   Map<String, Codec<? extends T>> codecs) {
		return createDispatched(registryKey, id, codecs, null);
	}

	static <R> RegistryEntryAttribute<R, Boolean> createBool(RegistryKey<Registry<R>> registryKey, Identifier id, boolean defaultValue) {
		return create(registryKey, id, Codec.BOOL, defaultValue);
	}

	static <R> RegistryEntryAttribute<R, Integer> createInt(RegistryKey<Registry<R>> registryKey, Identifier id, int defaultValue) {
		return create(registryKey, id, Codec.INT, defaultValue);
	}

	static <R> RegistryEntryAttribute<R, Long> createLong(RegistryKey<Registry<R>> registryKey, Identifier id, long defaultValue) {
		return create(registryKey, id, Codec.LONG, defaultValue);
	}

	static <R> RegistryEntryAttribute<R, Float> createFloat(RegistryKey<Registry<R>> registryKey, Identifier id, float defaultValue) {
		return create(registryKey, id, Codec.FLOAT, defaultValue);
	}

	static <R> RegistryEntryAttribute<R, Double> createDouble(RegistryKey<Registry<R>> registryKey, Identifier id, double defaultValue) {
		return create(registryKey, id, Codec.DOUBLE, defaultValue);
	}

	static <R> RegistryEntryAttribute<R, Boolean> createBool(RegistryKey<Registry<R>> registryKey, Identifier id) {
		return create(registryKey, id, Codec.BOOL, null);
	}

	static <R> RegistryEntryAttribute<R, Integer> createInt(RegistryKey<Registry<R>> registryKey, Identifier id) {
		return create(registryKey, id, Codec.INT, null);
	}

	static <R> RegistryEntryAttribute<R, Long> createLong(RegistryKey<Registry<R>> registryKey, Identifier id) {
		return create(registryKey, id, Codec.LONG, null);
	}

	static <R> RegistryEntryAttribute<R, Float> createFloat(RegistryKey<Registry<R>> registryKey, Identifier id) {
		return create(registryKey, id, Codec.FLOAT, null);
	}

	static <R> RegistryEntryAttribute<R, Double> createDouble(RegistryKey<Registry<R>> registryKey, Identifier id) {
		return create(registryKey, id, Codec.DOUBLE, null);
	}

	RegistryKey<Registry<R>> getRegistryKey();
	Identifier getId();
	Codec<T> getCodec();
	@Nullable T getDefaultValue();
}
