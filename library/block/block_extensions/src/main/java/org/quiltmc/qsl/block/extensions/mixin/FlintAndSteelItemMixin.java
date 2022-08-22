package org.quiltmc.qsl.block.extensions.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

import org.quiltmc.qsl.block.extensions.api.event.BlockInteractionEvents;

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
		var result = BlockInteractionEvents.IGNITE.invoker().onBlockIgnited(context);
		if (result != ActionResult.PASS) {
			cir.setReturnValue(result);
		}
	}
}
