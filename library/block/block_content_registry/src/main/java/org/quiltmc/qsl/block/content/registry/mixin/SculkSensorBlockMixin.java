package org.quiltmc.qsl.block.content.registry.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.SculkSensorBlock;
import net.minecraft.world.event.GameEvent;

@Mixin(SculkSensorBlock.class)
public class SculkSensorBlockMixin {
    @Final @Mutable @Shadow
    public static Object2IntMap<GameEvent> EVENTS;

    @Inject(method = "<clinit>", at= @At("TAIL"))
    private static void changeMap(CallbackInfo ci) {
        EVENTS = new Object2IntOpenHashMap<>(EVENTS);
    }
}
