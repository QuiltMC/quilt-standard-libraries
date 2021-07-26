package org.quiltmc.qsl.inworldrecipes.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {
	@Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
	public void qsl$useOnBlockOverride(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
		ActionResult result = qsl$useOnBlock(context);
		if (result != ActionResult.PASS)
			cir.setReturnValue(result);
	}

	public @NotNull ActionResult qsl$useOnBlock(@NotNull ItemUsageContext context) {
		return ActionResult.PASS;
	}
}
