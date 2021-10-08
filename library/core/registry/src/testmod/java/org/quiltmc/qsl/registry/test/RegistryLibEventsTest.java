package org.quiltmc.qsl.registry.test;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.qsl.registry.api.event.RegistryEvents;

public class RegistryLibEventsTest implements ModInitializer {
	private static final Logger LOG = LogManager.getLogger("Quilt Registry Lib Events Test");

	private static final Identifier TEST_BLOCK_ID = new Identifier("quilt_registry_test_events", "event_test_block");

	private static boolean entryAddEventFoundBlock = false;

	@Override
	public void onInitialize() {
		RegistryEvents.getEntryAddEvent(Registry.BLOCK).register(context -> {
			LOG.info("Block {} id={} raw={} was registered in registry {}\n",
					context.entry(), context.id(), context.rawId(), context.registry());

			if (TEST_BLOCK_ID.equals(context.id())) {
				entryAddEventFoundBlock = true;
			}
		});

		Registry.register(Registry.BLOCK, TEST_BLOCK_ID, new Block(AbstractBlock.Settings.of(Material.STONE, MapColor.BLACK)));

		if (!entryAddEventFoundBlock) {
			throw new AssertionError("Registry entry add event was not invoked on the registration of block with id " + TEST_BLOCK_ID);
		}
	}
}
