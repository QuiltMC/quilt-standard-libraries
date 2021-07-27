package org.quiltmc.qsl.toolinteractionrecipes.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.toolinteractionrecipes.impl.ToolInteractionRecipeCollections;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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
		// TODO replace with an event listener once Quilt has an item use event
		Identifier toolTypeId = qsl$getToolTypeId(item);
		if (toolTypeId == null)
			return;
		if (ToolInteractionRecipeCollections.get(context.getWorld()).getCollection(toolTypeId).tryPerform(context))
			cir.setReturnValue(ActionResult.SUCCESS);
	}

	// FIXME TEMPORARY UNTIL TOOL ATTRIBUTE API IS AVAILABLE
	@Unique private static @Nullable Identifier qsl$getToolTypeId(@NotNull Item item) {
		if (item instanceof SwordItem)
			return new Identifier("minecraft", "sword");
		if (item instanceof PickaxeItem)
			return new Identifier("minecraft", "pickaxe");
		if (item instanceof AxeItem)
			return new Identifier("minecraft", "axe");
		if (item instanceof ShovelItem)
			return new Identifier("minecraft", "shovel");
		if (item instanceof HoeItem)
			return new Identifier("minecraft", "hoe");
		return null;
	}
}
