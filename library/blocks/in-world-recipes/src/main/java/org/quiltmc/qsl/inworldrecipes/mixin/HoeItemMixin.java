package org.quiltmc.qsl.inworldrecipes.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
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

@Mixin(HoeItem.class)
public abstract class HoeItemMixin {
	@Inject(method = "useOnBlock",
			at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0),
			cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	public void doCustomRecipes(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir, World world, BlockPos blockPos) {
		if (!InWorldRecipeRegistries.tryPerform(InWorldRecipeRegistries.AXE, context, world.getBlockState(blockPos).getBlock()))
			return;
		if (!world.isClient) {
			PlayerEntity playerEntity = context.getPlayer();
			if (playerEntity != null) {
				context.getStack().damage(1, playerEntity, (p) -> p.sendToolBreakStatus(context.getHand()));
			}
		}
		cir.setReturnValue(ActionResult.success(world.isClient));
	}
}
