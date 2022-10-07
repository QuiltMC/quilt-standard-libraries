package org.quiltmc.qsl.entity.effect.mixin;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;

import org.quiltmc.qsl.entity.effect.api.QuiltStatusEffectExtensions;
import org.quiltmc.qsl.entity.effect.api.StatusEffectRemovalReason;

@Mixin(StatusEffect.class)
public abstract class StatusEffectMixin implements QuiltStatusEffectExtensions {
	@Shadow
	public abstract void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier);

	@Override
	public void onRemoved(@NotNull LivingEntity entity, @NotNull AttributeContainer attributes, int amplifier, @NotNull StatusEffectRemovalReason reason) {
		this.onRemoved(entity, attributes, amplifier);
	}
}
