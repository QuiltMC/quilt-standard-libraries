package org.quiltmc.qsl.registry.test;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.qsl.registry.event.api.RegistryMonitor;

import java.util.ArrayList;

public class RegistryLibMonitorTest implements ModInitializer {
	private static final Logger LOG = LogManager.getLogger("Quilt Registry Lib Monitor Test");

	private static final Identifier TEST_BLOCK_A_ID = new Identifier("quilt_registry_test_monitors", "test_block_a");
	private static final Identifier TEST_BLOCK_B_ID = new Identifier("quilt_registry_test_monitors", "test_block_b");

	@Override
	public void onInitialize() {
		Registry.register(Registry.BLOCK, TEST_BLOCK_A_ID, new Block(AbstractBlock.Settings.of(Material.STONE, MapColor.BLACK)));

		var monitor = RegistryMonitor.create(Registry.BLOCK)
				.withFilter((entry, id, rawId) -> id.getNamespace().equals("quilt_registry_test_monitors"));

		var allList = new ArrayList<Block>();
		var upcomingList = new ArrayList<Block>();

		monitor.forAll((registry, entry, id, rawId) -> {
			LOG.info("[forAll event]: Block {} id={} raw={} had its registration monitored in registry {}\n", entry, id, rawId, registry);
			allList.add(entry);
		});
		monitor.forUpcoming((registry, entry, id, rawId) -> {
			LOG.info("[forUpcoming event]: Block {} id={} raw={} had its registration monitored in registry {}\n", entry, id, rawId, registry);
			upcomingList.add(entry);
		});

		Registry.register(Registry.BLOCK, TEST_BLOCK_B_ID, new Block(AbstractBlock.Settings.of(Material.STONE, MapColor.BLACK)));

		if (allList.size() != 2) {
			throw new AssertionError("Entries " + allList + " found by RegistryMonitor via forAll were not as expected");
		}
		if (upcomingList.size() != 1) {
			throw new AssertionError("Entries " + upcomingList + " found by RegistryMonitor via forUpcoming were not as expected");
		}
	}
}
