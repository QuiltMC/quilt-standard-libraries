package org.quiltmc.qsl.entity.interaction.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.quiltmc.qsl.entity.interaction.api.player.UseBlockCallback;
import org.quiltmc.qsl.entity.interaction.api.player.UseItemCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

	@Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
	private void onItemInteract(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		if (player.isSpectator()) return;

		TypedActionResult<ItemStack> result = UseItemCallback.EVENT.invoker().onUse(player, world, hand);

		if (result.getResult() != ActionResult.PASS) cir.setReturnValue(result.getResult());
	}

	@Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
	private void onBlockInteract(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
		ActionResult result = UseBlockCallback.EVENT.invoker().onUseBlock(player, world, hand, hitResult);
		
		if (result != ActionResult.PASS) cir.setReturnValue(result);
	}
}
