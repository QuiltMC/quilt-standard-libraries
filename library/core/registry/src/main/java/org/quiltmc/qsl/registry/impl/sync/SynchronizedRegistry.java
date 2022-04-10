package org.quiltmc.qsl.registry.impl.sync;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import org.quiltmc.qsl.registry.api.sync.RegistryFlag;

import java.util.Collection;
import java.util.Map;

public interface SynchronizedRegistry<T> {
	void quilt$markForSync();

	boolean quilt$requiresSyncing();

	Map<String, Collection<SyncEntry>> quilt$getSyncMap();

	void quilt$markDirty();

	void quilt$createIdSnapshot();

	void quilt$restoreIdSnapshot();

	Collection<MissingEntry> quilt$applySyncMap(Map<String, Collection<SyncEntry>> map);

	static <T> SynchronizedRegistry<T> as(SimpleRegistry<T> registry) {
		return (SynchronizedRegistry) registry;
	}

	static void markForSync(Registry<?>... registries) {
		for (var reg : registries) {
			if (reg instanceof SynchronizedRegistry synchronizedRegistry) {
				synchronizedRegistry.quilt$markForSync();
			}
		}
	}

	void quilt$setRegistryFlag(RegistryFlag flag);

	byte quilt$getRegistryFlag();

	void quilt$setEntryFlag(T o, RegistryFlag flag);

	record SyncEntry(String path, int rawId, byte flags) {};
	record MissingEntry(Identifier identifier, int rawId, byte flags) {};
}
