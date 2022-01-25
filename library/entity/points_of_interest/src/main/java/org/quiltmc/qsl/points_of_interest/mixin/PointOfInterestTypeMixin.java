package org.quiltmc.qsl.points_of_interest.mixin;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Util;
import net.minecraft.world.poi.PointOfInterestType;
import org.quiltmc.qsl.points_of_interest.impl.PointOfInterestTypeExtensions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Mixin(PointOfInterestType.class)
public class PointOfInterestTypeMixin implements PointOfInterestTypeExtensions {

	@Shadow
	@Final
	@Mutable
	private Set<BlockState> blockStates;

	@Shadow
	@Final
	private static Map<BlockState, PointOfInterestType> BLOCK_STATE_TO_POINT_OF_INTEREST_TYPE;

	@Override
	public void addBlocks(Collection<Block> blocks) {
		final ImmutableSet.Builder<BlockState> builder = ImmutableSet.builder();

		for (Block block : blocks) {
			Set<BlockState> validStates = Sets.difference(Set.copyOf(block.getStateManager().getStates()), this.blockStates);
			builder.addAll(validStates);
			for (BlockState state : validStates) {
				PointOfInterestType replaced = BLOCK_STATE_TO_POINT_OF_INTEREST_TYPE.put(state, (PointOfInterestType) (Object) this);
				if (replaced != null) {
					throw Util.throwOrPause(new IllegalStateException(String.format("%s is defined in too many tags", state)));
				}
			}
		}

		this.blockStates = builder.addAll(this.blockStates).build();
	}

	@Override
	public void addBlockStates(Collection<BlockState> states) {
		final ImmutableSet.Builder<BlockState> builder = ImmutableSet.builder();

		Set<BlockState> validStates = Sets.difference(Set.copyOf(states), this.blockStates);
		builder.addAll(validStates);
		for (BlockState state : validStates) {
			PointOfInterestType replaced = BLOCK_STATE_TO_POINT_OF_INTEREST_TYPE.put(state, (PointOfInterestType) (Object) this);
			if (replaced != null) {
				throw Util.throwOrPause(new IllegalStateException(String.format("%s is defined in too many tags", state)));
			}
		}

		this.blockStates = builder.addAll(this.blockStates).build();
	}
}
