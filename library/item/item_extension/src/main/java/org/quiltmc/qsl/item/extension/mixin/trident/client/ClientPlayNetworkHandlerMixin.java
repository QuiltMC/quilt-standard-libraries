package org.quiltmc.qsl.item.extension.mixin.trident.client;

import org.quiltmc.qsl.item.extension.impl.trident.TridentClientModInitializer;
import org.quiltmc.qsl.item.extension.mixin.trident.TridentEntityAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onEntitySpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onSpawnPacket(Lnet/minecraft/network/packet/s2c/play/EntitySpawnS2CPacket;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onEntitySpawnMixin(EntitySpawnS2CPacket packet, CallbackInfo ci, EntityType<?> entityType, Entity entity) {
        if (entityType == EntityType.TRIDENT) {
            ((TridentEntityAccessor) entity).setTridentStack(TridentClientModInitializer.TRIDENT_QUEUE.remove());
        }
    }
}
