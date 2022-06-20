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
		ProtoChunk ret = cir.getReturnValue();
		NbtCompound rootQslNbt = nbt.getCompound(StringConstants.COMPONENT_ROOT);

		if (ret instanceof ReadOnlyChunk readOnly) {
			((NbtComponentProvider) readOnly.getWrappedChunk()).getNbtComponents().forEach((id, component) -> NbtComponent.readFrom(component, id, rootQslNbt));
		} else {
			((NbtComponentProvider) ret).getNbtComponents().forEach((id, component) -> NbtComponent.readFrom(component, id, rootQslNbt));
		}

		cir.setReturnValue(ret);
	}

	@Inject(method = "serialize", at = @At("RETURN"), cancellable = true)
	private static void serializeComponents(ServerWorld world, Chunk chunk, CallbackInfoReturnable<NbtCompound> cir) {
		var rootQslNbt = new NbtCompound();
		((NbtComponentProvider) chunk).getNbtComponents().forEach((id, component) -> NbtComponent.writeTo(rootQslNbt, component, id));
		NbtCompound ret = cir.getReturnValue();
		ret.put(StringConstants.COMPONENT_ROOT, rootQslNbt);
		cir.setReturnValue(ret);
	}
}
