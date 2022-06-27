package org.quiltmc.qsl.component.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldChunk.DirectBlockEntityTickInvoker.class)
public class MixinDirectBlockEntityTickInvoker<T extends BlockEntity> {
	@Shadow
	@Final
	private T blockEntity;

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntityTicker;tick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/BlockEntity;)V"))
	private void tickContainer(BlockEntityTicker<T> instance, World world, BlockPos blockPos, BlockState blockState, T t) {
		instance.tick(world, blockPos, blockState, t);
		this.blockEntity.getContainer().tick(this.blockEntity);
	}
}
