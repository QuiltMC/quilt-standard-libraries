package org.quiltmc.qsl.component.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSerializer.class)
public class MixinChunkSerializer {
	@Inject(method = "deserialize", at = @At("RETURN"), cancellable = true) // FIXME: More chunk issues
	private static void deserializeComponents(ServerWorld world, PointOfInterestStorage poiStorage, ChunkPos pos, NbtCompound nbt, CallbackInfoReturnable<ProtoChunk> cir) {
		var ret = cir.getReturnValue();
		var target = ret instanceof ReadOnlyChunk readOnly ? readOnly.getWrappedChunk() : ret;
		((ComponentProvider) target).getContainer().readNbt(nbt);
		cir.setReturnValue(ret);
	}

	@Inject(method = "serialize", at = @At("RETURN"), cancellable = true)
	private static void serializeComponents(ServerWorld world, Chunk chunk, CallbackInfoReturnable<NbtCompound> cir) {
		var ret = cir.getReturnValue();
		((ComponentProvider) chunk).getContainer().writeNbt(ret);
		cir.setReturnValue(ret);
	}
}
