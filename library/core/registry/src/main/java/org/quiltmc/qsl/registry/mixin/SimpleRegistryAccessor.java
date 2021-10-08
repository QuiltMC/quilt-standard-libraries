package org.quiltmc.qsl.registry.mixin;

import com.google.common.collect.BiMap;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SimpleRegistry.class)
public interface SimpleRegistryAccessor<V> {
	@Accessor("idToEntry")
	BiMap<Identifier, V> getIdToEntryMap();
}
