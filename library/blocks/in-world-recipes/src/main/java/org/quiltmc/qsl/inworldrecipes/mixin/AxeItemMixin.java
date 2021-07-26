package org.quiltmc.qsl.inworldrecipes.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.quiltmc.qsl.inworldrecipes.api.InWorldRecipeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(AxeItem.class)
public abstract class AxeItemMixin {
	@Inject(method = "useOnBlock",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/item/AxeItem;getStrippedState(Lnet/minecraft/block/BlockState;)Ljava/util/Optional;"),
			cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	public void qsl$doCustomRecipes(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir,
									World world, BlockPos blockPos, PlayerEntity playerEntity, BlockState blockState) {
		if (!InWorldRecipeRegistries.tryPerform(InWorldRecipeRegistries.AXE, context, blockState.getBlock()))
			return;
		if (!world.isClient) {
			if (playerEntity != null) {
				context.getStack().damage(1, playerEntity, (p) -> p.sendToolBreakStatus(context.getHand()));
			}
		}
		cir.setReturnValue(ActionResult.success(world.isClient));
	}
}
