package org.quiltmc.qsl.item.events.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;

import org.quiltmc.qsl.item.events.impl.CachedBlockPositionExtensions;

@Mixin(CachedBlockPosition.class)
public abstract class CachedBlockPositionMixin implements CachedBlockPositionExtensions {
	@Shadow
	private @Nullable BlockState state;

	@Shadow
	private @Nullable BlockEntity blockEntity;

	@Override
	public void quilt$markDirty() {
		this.state = null;
		this.blockEntity = null;
	}
}
