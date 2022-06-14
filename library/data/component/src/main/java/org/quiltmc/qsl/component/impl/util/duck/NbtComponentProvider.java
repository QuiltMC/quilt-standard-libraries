package org.quiltmc.qsl.component.impl.util.duck;

import com.google.common.collect.ImmutableMap;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.component.api.components.NbtComponent;

public interface NbtComponentProvider {
	ImmutableMap<Identifier, NbtComponent<?>> get();
}
