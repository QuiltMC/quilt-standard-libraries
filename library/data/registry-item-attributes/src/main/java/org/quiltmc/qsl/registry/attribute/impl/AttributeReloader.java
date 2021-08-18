package org.quiltmc.qsl.registry.attribute.impl;

import org.quiltmc.qsl.resource.loader.api.reloader.SimpleSynchronousResourceReloader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import java.util.Map;

public final class AttributeReloader implements SimpleSynchronousResourceReloader {
	private final Identifier ID = new Identifier("quilt", "attributes");

	@Override
	public Identifier getQuiltId() {
		return ID;
	}

	@Override
	public void reload(ResourceManager manager) {
		for (Map.Entry<? extends RegistryKey<? extends Registry<?>>, ? extends Registry<?>> entry : Registry.REGISTRIES.getEntries()) {
			Identifier id = entry.getKey().getValue();
			StringBuilder pathSB = new StringBuilder("attributes/");
			if (!"minecraft".equals(id.getNamespace()))
				pathSB.append(id.getNamespace()).append('/');
			pathSB.append(id.getPath());
			manager.findResources(pathSB.toString(), s -> s.endsWith(".json")).forEach(System.out::println);
		}
	}
}
