package org.quiltmc.qsl.item.extensions.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

import org.quiltmc.qsl.item.extensions.api.event.ItemInteractionEvents;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@Redirect(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;"))
	private ActionResult quilt$invokeUsedOnBlockEvent(Item instance, ItemUsageContext context) {
		var result = ItemInteractionEvents.USED_ON_BLOCK.invoker().onItemUsedOnBlock(context);
		if (result == ActionResult.PASS) {
			result = instance.useOnBlock(context);
		}
		return result;
	}
}
