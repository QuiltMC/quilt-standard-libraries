package org.quiltmc.qsl.advancement.api;

import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.util.Identifier;

public interface QuiltCriteria {
	<T extends Criterion<?>> T register(Identifier id, T criterion);
}
