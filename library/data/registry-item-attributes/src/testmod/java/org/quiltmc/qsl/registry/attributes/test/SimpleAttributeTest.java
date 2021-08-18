package org.quiltmc.qsl.registry.attributes.test;

import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;

import org.quiltmc.qsl.registry.attribute.api.RegistryItemAttribute;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SimpleAttributeTest implements ModInitializer {
	public static final RegistryItemAttribute<Item, Integer> TEST_ATTRIBUTE =
			RegistryItemAttribute.create(Registry.ITEM_KEY,
					new Identifier("quilt_registry_item_attributes_testmod", "test_attribute"),
					Codec.INT,
					0);

	@Override
	public void onInitialize() {

	}
}
