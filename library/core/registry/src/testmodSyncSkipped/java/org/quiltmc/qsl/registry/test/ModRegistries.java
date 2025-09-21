package org.quiltmc.qsl.registry.test;

import static org.quiltmc.qsl.registry.test.RegistryLibBuilderTests.id;

import net.fabricmc.api.EnvType;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.registry.api.QuiltRegistryBuilder;

public class ModRegistries {
	static {
		// horrible no good hack to make sure only the server is aware of the registry
		// (can't just use DedicatedServerModInitializer since that's invoked after registries are frozen)
		if (MinecraftQuiltLoader.getEnvironmentType() != EnvType.SERVER) {
			throw new RuntimeException("Client shouldn't be aware of GAS_TYPE!");
		}
	}

	public static final RegistryKey<Registry<GasType>> GAS_TYPE_KEY = RegistryKey.ofRegistry(id("gas_type"));

	public static final Registry<GasType> GAS_TYPE = QuiltRegistryBuilder.of(GAS_TYPE_KEY)
			.frozen()
			.syncRequired()
			.build();
}
