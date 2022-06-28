package org.quiltmc.qsl.component.impl.sync;

import com.mojang.serialization.Lifecycle;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.impl.CommonInitializer;
import org.quiltmc.qsl.component.impl.sync.header.SyncPacketHeader;

public class ComponentHeaderRegistry {
	public static final Registry<SyncPacketHeader<?>> HEADERS =
			new SimpleRegistry<>(RegistryKey.ofRegistry(CommonInitializer.id("sync_headers")), Lifecycle.experimental(), null);

	public static <P extends ComponentProvider> SyncPacketHeader<P> register(Identifier id, SyncPacketHeader<P> header) {
		return Registry.register(HEADERS, id, header);
	}
}
