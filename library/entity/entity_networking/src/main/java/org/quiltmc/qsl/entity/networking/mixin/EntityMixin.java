package org.quiltmc.qsl.entity.networking.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import org.quiltmc.qsl.entity.networking.api.custom_spawn_data.QuiltCustomSpawnDataEntity;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
	@Inject(method = "createSpawnPacket", at = @At("HEAD"), cancellable = true)
	private void quilt$handleCustomSpawnPacket(CallbackInfoReturnable<Packet<ClientPlayPacketListener>> cir) {
		if (this instanceof QuiltCustomSpawnDataEntity custom) {
			PacketByteBuf buf = PacketByteBufs.create();
			new EntitySpawnS2CPacket((Entity) (Object) this).write(buf);
			custom.writeCustomSpawnData(buf);
			Packet<ClientPlayPacketListener> packet = ServerPlayNetworking.createS2CPacket(
				QuiltCustomSpawnDataEntity.EXTENDED_SPAWN_PACKET, buf
			);
			cir.setReturnValue(packet);
		}
	}
}
