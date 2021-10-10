package org.quiltmc.qsl.registry.mixin;

import com.google.common.collect.BiMap;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Used to access the namespaced ID to entry map in a {@link net.minecraft.util.registry.Registry} to increase iteration
 * speed.
 */
@Mixin(SimpleRegistry.class)
public interface SimpleRegistryAccessor<V> {
	@Accessor("idToEntry")
	BiMap<Identifier, V> quilt$getIdToEntryMap();
}
