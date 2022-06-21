package org.quiltmc.qsl.component.mixin;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.chunk.BlendingData;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.impl.util.duck.NbtComponentProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(WorldChunk.class)
public abstract class MixinWorldChunk extends Chunk {
	public MixinWorldChunk(ChunkPos chunkPos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<Biome> registry, long l, @Nullable ChunkSection[] chunkSections, @Nullable BlendingData blendingData) {
		super(chunkPos, upgradeData, heightLimitView, registry, l, chunkSections, blendingData);
	}

	@Inject(method = "<init>(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/ProtoChunk;Lnet/minecraft/world/chunk/WorldChunk$PostLoadProcessor;)V", at = @At("TAIL"))
	private void copyComponentData(ServerWorld serverWorld, ProtoChunk protoChunk, WorldChunk.PostLoadProcessor postLoadProcessor, CallbackInfo ci) {
		var target = protoChunk instanceof ReadOnlyChunk readOnly ? readOnly.getWrappedChunk() : protoChunk;
		var this$ = (ComponentProvider) this;
		Map<Identifier, Component> componentMap = this$.exposeAll();
		componentMap.putAll(((ComponentProvider) target).exposeAll());
		((NbtComponentProvider) this$).getNbtComponents().forEach((id, component) ->
				component.setSaveOperation(() -> this.setNeedsSaving(true))
		);
	}
}
