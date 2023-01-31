package org.quiltmc.qsl.item.extension.mixin.trident;

import org.quiltmc.qsl.item.extension.impl.trident.TridentClientModInitializer;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.World;

@Mixin(ProjectileEntity.class)
public abstract class TridentEntityMixin extends Entity {
    public TridentEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "createSpawnPacket", at = @At("HEAD"))
    public void sendTridentStackBeforeSpawnPacket(CallbackInfoReturnable<Packet<?>> cir) {
        if ((Object) this instanceof TridentEntity trident) {
            PacketByteBuf passedData = PacketByteBufs.create();
            passedData.writeItemStack(((TridentEntityAccessor) trident).getTridentStack());
            ServerPlayNetworking.send(this.world.getServer().getPlayerManager().getPlayerList(), TridentClientModInitializer.TRIDENT_SPAWN_PACKET_ID, passedData);
        }
    }
}
