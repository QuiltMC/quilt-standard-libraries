package org.quiltmc.qsl.toolinteractionrecipes.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@Inject(method = "useOnBlock",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/item/Item;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;",
					ordinal = 0),
			cancellable = true, locals = LocalCapture.PRINT)
	public void qsl$tryPerformToolInteractionRecipe(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir,
													PlayerEntity playerEntity, Item item) {
		// TODO replace with an event listener once Quilt has an item use event (though this will require cross-module dep)
		// TODO figure this out
	}
}
