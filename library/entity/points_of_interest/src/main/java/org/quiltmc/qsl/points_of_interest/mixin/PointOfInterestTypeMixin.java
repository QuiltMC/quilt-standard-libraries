package org.quiltmc.qsl.points_of_interest.mixin;

import static net.minecraft.world.poi.PointOfInterestTypes.STATE_TO_TYPE;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Holder;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;

import org.quiltmc.qsl.points_of_interest.impl.PointOfInterestTypeExtensions;

@Mixin(PointOfInterestType.class)
public class PointOfInterestTypeMixin implements PointOfInterestTypeExtensions {
	@Mutable
	@Shadow
	@Final
	private Set<BlockState> blockStates;

	@Override
	public void addBlocks(RegistryKey<PointOfInterestType> key, Collection<Block> blocks) {
		ImmutableSet.Builder<BlockState> builder = new ImmutableSet.Builder<>();

		for (Block block : blocks) {
			builder.addAll(block.getStateManager().getStates());
		}

		this.setBlockStates(key, builder.build(), true);
	}

	@Override
	public void addBlockStates(RegistryKey<PointOfInterestType> key, Collection<BlockState> states) {
		ImmutableSet.Builder<BlockState> builder = new ImmutableSet.Builder<>();

		builder.addAll(states);

		this.setBlockStates(key, builder.build(), true);
	}

	@Override
	public void setBlocks(RegistryKey<PointOfInterestType> key, Collection<Block> blocks) {
		ImmutableSet.Builder<BlockState> builder = new ImmutableSet.Builder<>();

		for (Block block : blocks) {
			builder.addAll(block.getStateManager().getStates());
		}

		this.setBlockStates(key, builder.build(), false);
	}

	@Override
	public void setBlockStates(RegistryKey<PointOfInterestType> key, Collection<BlockState> states) {
		this.setBlockStates(key, states, false);
	}

	private void setBlockStates(RegistryKey<PointOfInterestType> key, Collection<BlockState> states, boolean added) {
		if (!added) {
			PointOfInterestTypes.ALL_STATES.removeAll(this.blockStates);

			for (BlockState state : this.blockStates) {
				STATE_TO_TYPE.remove(state);
			}
		}

		for (BlockState state : states) {
			Holder<PointOfInterestType> replaced = STATE_TO_TYPE.put(state, Registry.POINT_OF_INTEREST_TYPE.getHolderOrThrow(key));
			if (replaced != null) {
				throw Util.throwOrPause(new IllegalStateException(String.format("%s is defined in too many tags", state)));
			}
		}

		PointOfInterestTypes.ALL_STATES.addAll(states);

		this.blockStates = Set.copyOf(states);
	}
}
