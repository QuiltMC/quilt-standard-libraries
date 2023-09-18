package org.quiltmc.qsl.advancement.mixin;


import com.google.common.collect.BiMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.util.Identifier;

@Mixin(Criteria.class)
public interface CriteriaAccessor {
	@Accessor("VALUES")
	static BiMap<Identifier, Criterion<?>> values() {
		throw new IllegalStateException("Mixin Accessor");
	}
}
