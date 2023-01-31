package org.quiltmc.qsl.item.extension.mixin.trident.client;

import org.quiltmc.qsl.item.extension.api.trident.TridentExtensions;
import org.quiltmc.qsl.item.extension.mixin.trident.TridentEntityAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.entity.TridentEntityRenderer;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.util.Identifier;

@Mixin(TridentEntityRenderer.class)
public class TridentEntityRendererMixin {
    @Inject(method = "getTexture(Lnet/minecraft/entity/projectile/TridentEntity;)Lnet/minecraft/util/Identifier;", at = @At(value = "HEAD"), cancellable = true)
    public void getTextureMixin(TridentEntity entity, CallbackInfoReturnable<Identifier> cir) {
        if(((TridentEntityAccessor) entity).getTridentStack().getItem() instanceof TridentExtensions tridentItem) {
            cir.setReturnValue(tridentItem.getRenderTexture());
        }
    }
}
