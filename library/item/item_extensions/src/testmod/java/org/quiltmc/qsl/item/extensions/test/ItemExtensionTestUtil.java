package org.quiltmc.qsl.item.extensions.test;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class ItemExtensionTestUtil {
    private ItemExtensionTestUtil() {
        throw new UnsupportedOperationException("ItemExtensionTestUtil has only static members");
    }

    public static final String NAMESPACE = "quilt_item_extensions_testmod";

    public static Identifier createId(String path) {
        return Identifier.of(NAMESPACE, path);
    }

    public static RegistryKey<Item> createItemKey(String path) {
        return RegistryKey.of(RegistryKeys.ITEM, createId(path));
    }
}
