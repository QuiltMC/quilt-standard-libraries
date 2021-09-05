package org.quiltmc.qsl.registry.attribute.test.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttribute;

public class ClientAttributeTest implements ClientModInitializer {
	public static final RegistryEntryAttribute<Block, Boolean> ATTRIBUTE =
			RegistryEntryAttribute.boolBuilder(Registry.BLOCK, new Identifier("quilt", "test_attribute_client"))
					.side(RegistryEntryAttribute.Side.CLIENT).build();

	@Override
	public void onInitializeClient() { }
}
