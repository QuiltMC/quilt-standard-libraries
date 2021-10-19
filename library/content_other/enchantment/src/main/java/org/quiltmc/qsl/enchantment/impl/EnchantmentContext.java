package org.quiltmc.qsl.enchantment.impl;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * A class that contains contextual information about the enchantment process
 */
public class EnchantmentContext {

	// An integer representing the current enchantment level being queried (ie. I, II, III, etc.)
	int level;
	// The current power of the enchantment table (the amount of bookcases)
	int power;
	// The amount of bookcases around the enchantment table
	int bookcases;
	// The item stack for the enchantment to be applied to
	ItemStack stack;
	// The world in which the item is being enchanted
	World world;
	// The player currently trying to enchant the item
	PlayerEntity player;
	// The block position of the enchantment table
	BlockPos pos;
	// The block state of the enchantment table
	BlockState state;
	// The block entity of the enchantment table
	@Nullable BlockEntity entity;

	public EnchantmentContext(int level, int power, int bookcases, ItemStack stack, World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity entity) {
		this.level = level;
		this.power = power;
		this.bookcases = bookcases;
		this.stack = stack;
		this.world = world;
		this.player = player;
		this.pos = pos;
		this.state = state;
		this.entity = entity;
	}

	public EnchantmentContext withLevel(int level) {
		return new EnchantmentContext(level, this.power, this.bookcases, this.stack, this.world, this.player, this.pos, this.state, this.entity);
	}

	public int getLevel() {
		return this.level;
	}

	public int getPower() {
		return this.power;
	}

	public ItemStack getStack() {
		return this.stack;
	}

	public World getWorld() {
		return this.world;
	}

	public PlayerEntity getPlayer() {
		return this.player;
	}

	public BlockPos getPos() {
		return this.pos;
	}

	public BlockState getState() {
		return this.state;
	}

	public int getBookcases() {
		return this.bookcases;
	}

	@Nullable
	public BlockEntity getBlockEntity() {
		return this.entity;
	}

	public EnchantmentContext withPower(int power) {
		return new EnchantmentContext(this.level, power, this.bookcases, this.stack, this.world, this.player, this.pos, this.state, this.entity);
	}
}

