package org.quiltmc.qsl.component.impl.util.duck;

import net.minecraft.util.Identifier;
import org.quiltmc.qsl.component.api.Component;

import java.util.Map;

public interface CustomChunk {
	void setComponents(Map<Identifier, Component> components);
}
