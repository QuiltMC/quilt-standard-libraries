package org.quiltmc.qsl.registry.attribute.api;

import org.quiltmc.qsl.registry.attribute.impl.BuiltinRegistryItemAttributeSetterImpl;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import java.util.function.Consumer;

public final class RegistryExtensions {
	public static <R> void registerWithAttributes(Registry<R> registry, Identifier id, R item,
												  Consumer<ItemAttributeSetter<R>> setterConsumer) {
		Registry.register(registry, id, item);
		setterConsumer.accept(new BuiltinRegistryItemAttributeSetterImpl<>(registry, item));
	}

	public interface ItemAttributeSetter<R> {
		<T> ItemAttributeSetter<R> put(RegistryItemAttribute<R, T> attrib, T value);
	}
}
