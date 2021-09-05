package org.quiltmc.qsl.crash.test.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {
	@Inject(method = "tick", at=@At("HEAD"))
	private static void crashOnTick(World world, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {
		if (world.getBlockState(pos.down()).getBlock() == Blocks.DIAMOND_BLOCK) {
			world.removeBlock(pos, false);
			throw new RuntimeException("Crash Test!");
		}
	}
}
