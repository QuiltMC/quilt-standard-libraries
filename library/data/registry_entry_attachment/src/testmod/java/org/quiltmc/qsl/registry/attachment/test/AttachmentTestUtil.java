package org.quiltmc.qsl.registry.attachment.test;

import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;
import org.quiltmc.qsl.registry.attachment.api.RegistryExtensions;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public final class AttachmentTestUtil {
	private AttachmentTestUtil() {
		throw new AssertionError(AttachmentTestUtil.class.getSimpleName() + " contains only static members");
	}

	public static final String NAMESPACE = "quilt";

	public static Identifier createId(String path) {
		return Identifier.of(NAMESPACE, path);
	}

	public static RegistryKey<Item> createItemKey(String path) {
		return RegistryKey.of(RegistryKeys.ITEM, createId(path));
	}

	public static <I extends Item> I registerItem(String path, Function<Item.Settings, I> factory) {
		final RegistryKey<Item> key = createItemKey(path);
		return Registry.register(Registries.ITEM, key, factory.apply(new Item.Settings().key(key)));
	}

	public static <I extends Item, V> I registerItemWithExtension(
		String path, Function<Item.Settings, I> factory, RegistryEntryAttachment<Item, V> attachment, V value
	) {
		final RegistryKey<Item> key = createItemKey(path);
		return RegistryExtensions.register(
			Registries.ITEM, key.getValue(),
			factory.apply(new Item.Settings().key(key)),
			attachment, value
		);
	}
}
