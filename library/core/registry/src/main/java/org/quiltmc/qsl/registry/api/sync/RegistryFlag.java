package org.quiltmc.qsl.registry.api.sync;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.SimpleRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.registry.impl.sync.SynchronizedRegistry;

@ApiStatus.Experimental
public enum RegistryFlag {
	OPTIONAL,
	SKIP;

	public static boolean isOptional(byte flag) {
		return (flag >>> OPTIONAL.ordinal() & 0x1) == 1;
	}

	public static boolean isSkipped(byte flag) {
		return (flag >>> SKIP.ordinal() & 0x1) == 1;
	}

	public static void setRegistry(SimpleRegistry<?> registry, RegistryFlag flag) {
		SynchronizedRegistry.as(registry).quilt$setRegistryFlag(flag);
	}

	public static void setEntry(SimpleRegistry<?> registry, Identifier identifier, RegistryFlag flag) {
		SynchronizedRegistry.as((SimpleRegistry<Object>) registry).quilt$setEntryFlag(registry.get(identifier), flag);
	}

	public static <T> void setEntry(SimpleRegistry<T> registry, T entry, RegistryFlag flag) {
		SynchronizedRegistry.as(registry).quilt$setEntryFlag(entry, flag);
	}
}
