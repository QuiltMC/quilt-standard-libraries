package org.quiltmc.qsl.registry.attribute.api;

import org.quiltmc.qsl.registry.attribute.impl.BuiltinRegistryEntryAttributeSetterImpl;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import java.util.function.Consumer;

public final class RegistryExtensions {
	public static <R> void registerWithAttributes(Registry<R> registry, Identifier id, R item,
												  Consumer<AttributeSetter<R>> setterConsumer) {
		Registry.register(registry, id, item);
		setterConsumer.accept(new BuiltinRegistryEntryAttributeSetterImpl<>(registry, item));
	}

	public interface AttributeSetter<R> {
		<T> AttributeSetter<R> put(RegistryEntryAttribute<R, T> attrib, T value);
	}
}
