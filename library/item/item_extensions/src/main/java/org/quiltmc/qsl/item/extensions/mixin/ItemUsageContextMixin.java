package org.quiltmc.qsl.item.extensions.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import org.quiltmc.qsl.item.extensions.api.QuiltItemUsageContextExtensions;

@Mixin(ItemUsageContext.class)
public abstract class ItemUsageContextMixin implements QuiltItemUsageContextExtensions {
	@Shadow
	@Final
	private @Nullable PlayerEntity player;

	@Shadow
	@Final
	private Hand hand;

	@Shadow
	@Final
	private ItemStack stack;

	@Shadow
	@Final
	private World world;

	@Shadow
	public abstract BlockPos getBlockPos();

	@Override
	public void damageStack(int amount) {
		if (this.player != null) {
			this.stack.damage(amount, player, playerx -> playerx.sendToolBreakStatus(hand));
		}
	}

	@Override
	public void replaceBlock(BlockState newState) {
		var pos = this.getBlockPos();
		this.world.setBlockState(pos, newState, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
		this.world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
	}

	@Override
	public <T extends Comparable<T>> void setBlockProperty(Property<T> property, T newValue) {
		var pos = this.getBlockPos();
		var state = this.world.getBlockState(pos);
		state = state.with(property, newValue);
		this.replaceBlock(state);
	}
}
