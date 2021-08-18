package org.quiltmc.qsl.registry.attribute.impl;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttribute;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public final class RegistryEntryAttributeImpl<R, T> implements RegistryEntryAttribute<R, T> {
	private final RegistryKey<Registry<R>> registryKey;
	private final Identifier id;
	private final Codec<T> codec;
	private final @Nullable T defaultValue;

	private RegistryEntryAttributeImpl(RegistryKey<Registry<R>> registryKey, Identifier id, Codec<T> codec, @Nullable T defaultValue) {
		this.registryKey = registryKey;
		this.id = id;
		this.codec = codec;
		this.defaultValue = defaultValue;
	}

	public static <R, T> RegistryEntryAttribute<R, T> create(RegistryKey<Registry<R>> registryKey, Identifier id, Codec<T> codec,
															 @Nullable T defaultValue) {
		var attrib = new RegistryEntryAttributeImpl<>(registryKey, id, codec, defaultValue);
		@SuppressWarnings("unchecked") var registry = (Registry<R>) Registry.REGISTRIES.get(registryKey.getValue());
		RegistryEntryAttributeHolderImpl.getBuiltin(registry).registerAttribute(attrib);
		return attrib;
	}

	@Override
	public RegistryKey<Registry<R>> getRegistryKey() {
		return registryKey;
	}

	@Override
	public Identifier getId() {
		return id;
	}

	@Override
	public Codec<T> getCodec() {
		return codec;
	}

	@Override
	public @Nullable T getDefaultValue() {
		return defaultValue;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RegistryEntryAttributeImpl<?, ?> that)) return false;
		return Objects.equals(registryKey, that.registryKey) && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(registryKey, id);
	}
}
