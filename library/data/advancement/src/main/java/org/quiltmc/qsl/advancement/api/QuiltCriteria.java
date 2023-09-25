package org.quiltmc.qsl.advancement.api;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.advancement.mixin.CriteriaAccessor;

public class QuiltCriteria {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static <T extends Criterion<?>> T register(Identifier id, T criterion) {
		if ("minecraft".equals(id.getNamespace())) {
			LOGGER.warn("An attempt to register a modded criteria with the vanilla id " + id + " was made");
		}

		if (CriteriaAccessor.values().containsKey(id)) {
			throw new IllegalArgumentException("Duplicate criterion id " + id);
		}

		CriteriaAccessor.values().put(id, criterion);
		return criterion;
	}
}
