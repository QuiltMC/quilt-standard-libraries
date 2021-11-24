package org.quiltmc.qsl.registry.attribute.impl;

import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;

/**
 * Simple guard class that prevents access to the
 * {@linkplain RegistryEntryAttributeHolder#getAssets(Registry) assets-based <code>RegistryEntryAttributeHolder</code> instance}
 * in a dedicated server environment.
 */
@ApiStatus.Internal
public final class AssetsHolderGuard {
	private static boolean allowed = false;

	public static void setAccessAllowed() {
		allowed = true;
	}

	public static void assertAccessAllowed() {
		if (!allowed) {
			throw new IllegalStateException("Access to assets-based registry entry attributes is not allowed here!");
		}
	}
}
