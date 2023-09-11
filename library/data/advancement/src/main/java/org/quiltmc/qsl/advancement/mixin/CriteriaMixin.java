package org.quiltmc.qsl.advancement.mixin;

import java.util.Set;

import com.google.common.collect.BiMap;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.advancement.api.QuiltCriteria;

@Mixin(Criteria.class)
public abstract class CriteriaMixin implements QuiltCriteria {
	@Shadow
	@Final
	private static BiMap<Identifier, Criterion<?>> VALUES;

	@Unique
	private static Set<Identifier> quilt$VANILLA_IDS;

	@Unique
	private static final Logger LOGGER = LogUtils.getLogger();

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void obtainVanillaValues(CallbackInfo ci) {
		quilt$VANILLA_IDS = Set.copyOf(VALUES.keySet());
	}

	@Inject(method = "register", at = @At("HEAD"))
	private static <T extends Criterion<?>> void warnOnModdedVanillaRegisters(String id, T criterion, CallbackInfoReturnable<T> cir) {
		var identifier = new Identifier(id);
		if (quilt$VANILLA_IDS.contains(identifier)) {
			LOGGER.warn("An attempt to register a modded criteria with the vanilla id " + identifier + " was made");
		}
	}

	@Override
	public <T extends Criterion<?>> T register(Identifier id, T criterion) {
		if (quilt$VANILLA_IDS.contains(id) || "minecraft".equals(id.getNamespace())) {
			LOGGER.warn("An attempt to register a modded criteria with the vanilla id " + id + " was made");
		}

		if (VALUES.containsKey(id)) {
			throw new IllegalArgumentException("Duplicate criterion id " + id);
		}

		return criterion;
	}
}
