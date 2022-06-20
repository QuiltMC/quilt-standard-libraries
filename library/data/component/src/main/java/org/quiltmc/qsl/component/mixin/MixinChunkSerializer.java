package org.quiltmc.qsl.component.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.quiltmc.qsl.component.api.components.NbtComponent;
import org.quiltmc.qsl.component.impl.util.StringConstants;
import org.quiltmc.qsl.component.impl.util.duck.NbtComponentProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSerializer.class)
public class MixinChunkSerializer {
	@Inject(method = "deserialize", at = @At("RETURN"), cancellable = true)
	private static void deserializeComponents(ServerWorld world, PointOfInterestStorage poiStorage, ChunkPos pos, NbtCompound nbt, CallbackInfoReturnable<ProtoChunk> cir) {
		Chunk ret = cir.getReturnValue();

		if (ret instanceof ReadOnlyChunk readOnly) {
			NbtCompound rootQslNbt = nbt.getCompound(StringConstants.COMPONENT_ROOT);
			((NbtComponentProvider) readOnly.getWrappedChunk()).get().forEach((id, component) -> NbtComponent.forward(component, id, rootQslNbt));
			cir.setReturnValue(readOnly);
		}
	}

	@Inject(method = "serialize", at = @At("RETURN"), cancellable = true)
	private static void serializeComponents(ServerWorld world, Chunk chunk, CallbackInfoReturnable<NbtCompound> cir) {
		var rootQslNbt = new NbtCompound();
		((NbtComponentProvider) chunk).get().forEach((id, component) -> rootQslNbt.put(id.toString(), component.write()));
		NbtCompound ret = cir.getReturnValue();
		ret.put(StringConstants.COMPONENT_ROOT, rootQslNbt);
		cir.setReturnValue(ret);
	}
}
