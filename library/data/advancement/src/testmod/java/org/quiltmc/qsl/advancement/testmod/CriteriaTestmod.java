package org.quiltmc.qsl.advancement.testmod;

import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.advancement.criterion.ImpossibleCriterion;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.advancement.api.QuiltCriteria;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class CriteriaTestmod implements ModInitializer {
	public static final Criterion<ImpossibleCriterion.Conditions> IMPOSSIBLE = QuiltCriteria.register(new Identifier("quilt", "impossible"), new ImpossibleCriterion());

	@Override
	public void onInitialize(ModContainer mod) {
	}
}
