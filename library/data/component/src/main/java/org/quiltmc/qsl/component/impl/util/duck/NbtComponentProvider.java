package org.quiltmc.qsl.component.impl.util.duck;

import net.minecraft.util.Identifier;
import org.quiltmc.qsl.component.api.components.NbtComponent;

import java.util.Map;

public interface NbtComponentProvider {
	Map<Identifier, NbtComponent<?>> getNbtComponents();
}
