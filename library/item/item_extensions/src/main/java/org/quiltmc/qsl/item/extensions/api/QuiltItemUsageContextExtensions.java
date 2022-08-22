package org.quiltmc.qsl.item.extensions.api;

import org.jetbrains.annotations.NotNull;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.random.RandomGenerator;

import org.quiltmc.qsl.base.api.util.InjectedInterface;

@InjectedInterface(ItemUsageContext.class)
public interface QuiltItemUsageContextExtensions {
	// impls are in ItemUsageContextMixin

	default boolean isClientWorld() {
		return false;
	}

	default @NotNull ActionResult success() {
		return ActionResult.success(this.isClientWorld());
	}

	default @NotNull RandomGenerator getWorldRandom() {
		return RandomGenerator.createLegacy();
	}

	default @NotNull BlockState getBlockState() {
		return Blocks.AIR.getDefaultState();
	}

	default void damageStack(int amount) {}
	default void damageStack() {
		damageStack(1);
	}

	default void replaceBlock(@NotNull BlockState newState) {}
	default <T extends Comparable<T>> void setBlockProperty(@NotNull Property<T> property, @NotNull T newValue) {}

	default void playSoundAtBlock(SoundEvent sound, SoundCategory category, float volume, float pitch) {}
}
