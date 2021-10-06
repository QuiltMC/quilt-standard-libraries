package org.quiltmc.qsl.registry.test;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.registry.event.api.RegistryEvents;

public class RegistryLibTestMod implements ModInitializer {
	private static final Identifier TEST_BLOCK_ID = new Identifier("quilt_registry", "test_block");
	private static boolean entryAddEventFoundBlock = false;

	@Override
	public void onInitialize() {
		RegistryEvents.getEntryAddEvent(Registry.BLOCK).register((registry, entry, id, rawId) -> {
			System.out.printf("Registered block %s id=%s raw=%s in registry %s\n", entry, id, rawId, registry);

			if (TEST_BLOCK_ID.equals(id)) {
				entryAddEventFoundBlock = true;
			}
		});

		Registry.register(Registry.BLOCK, TEST_BLOCK_ID, new Block(AbstractBlock.Settings.of(Material.STONE, MapColor.BLACK)));

		if (!entryAddEventFoundBlock) {
			throw new AssertionError("Registry entry add event was not invoked on the registration of block with id " + TEST_BLOCK_ID);
		}
	}
}
