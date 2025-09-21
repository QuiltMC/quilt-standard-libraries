package org.quiltmc.qsl.registry.test;

import static org.quiltmc.qsl.registry.test.RegistryLibBuilderTests.id;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

import org.quiltmc.qsl.registry.api.QuiltRegistryBuilder;

public class ModRegistries {
	public static final RegistryKey<Registry<GasType>> GAS_TYPE_KEY = RegistryKey.ofRegistry(id("gas_type"));

	public static final Registry<GasType> GAS_TYPE = QuiltRegistryBuilder.of(GAS_TYPE_KEY)
			.frozen()
			.syncRequired()
			.build();
}
