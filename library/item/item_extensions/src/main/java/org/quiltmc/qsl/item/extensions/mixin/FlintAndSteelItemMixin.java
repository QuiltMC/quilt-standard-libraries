package org.quiltmc.qsl.item.extensions.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

import org.quiltmc.qsl.item.extensions.api.event.ItemInteractionEvents;

@Mixin(FlintAndSteelItem.class)
public abstract class FlintAndSteelItemMixin {
	@Inject(
			method = "useOnBlock",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/block/CampfireBlock;canBeLit(Lnet/minecraft/block/BlockState;)Z"
			),
			cancellable = true
	)
	private void quilt$invokeBlockIgniteEvent(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
		var result = ItemInteractionEvents.IGNITE_BLOCK.invoker().onIgniteBlock(context);
		if (result != ActionResult.PASS) {
			cir.setReturnValue(result);
		}
	}
}
