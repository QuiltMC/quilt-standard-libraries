package org.quiltmc.qsl.component.mixin.sync;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.quiltmc.qsl.component.api.sync.SyncChannel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldChunk.class)
public class WorldChunkMixin {
	@Inject(method = "method_31716", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;readNbt(Lnet/minecraft/nbt/NbtCompound;)V"))
	private void initialBlockEntitySync(BlockPos blockPos, BlockEntityType<?> blockEntityType, NbtCompound nbtCompound, CallbackInfo ci) {
		SyncChannel.BLOCK_ENTITY.requestSync(blockPos);
	}
}
