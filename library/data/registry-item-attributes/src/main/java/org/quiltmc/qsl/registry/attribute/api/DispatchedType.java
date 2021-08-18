package org.quiltmc.qsl.registry.attribute.api;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;
import java.util.Map;

/**
 * Utility interface used for {@link RegistryItemAttribute#createDispatched(RegistryKey, Identifier, Map, DispatchedType)}.<p>
 *
 * This allows for polymorphic attribute types!
 */
public interface DispatchedType {
	String getType();
}
