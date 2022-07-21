package org.quiltmc.qsl.component.mixin.sync;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.ChunkPos;

import org.quiltmc.qsl.component.api.sync.SyncChannel;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
	@Inject(method = "onChunkData", at = @At("RETURN"))
	private void initialChunkSync(ChunkDataS2CPacket packet, CallbackInfo ci) {
		SyncChannel.CHUNK.requestSync(new ChunkPos(packet.chunkX(), packet.chunkZ()));
	}

	@Inject(method = "onEntitySpawn", at = @At("RETURN"))
	private void initialEntitySync(EntitySpawnS2CPacket packet, CallbackInfo ci) {
		SyncChannel.ENTITY.requestSync(packet.getId());
	}
}
