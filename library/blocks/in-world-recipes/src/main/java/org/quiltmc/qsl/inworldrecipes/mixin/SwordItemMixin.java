package org.quiltmc.qsl.inworldrecipes.mixin;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.SwordItem;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.quiltmc.qsl.inworldrecipes.api.InWorldRecipe;
import org.quiltmc.qsl.inworldrecipes.api.InWorldRecipeRegistries;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(SwordItem.class)
public abstract class SwordItemMixin extends Item {
	public SwordItemMixin() {
		super(new Settings());
		throw new AssertionError();
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		Block block = world.getBlockState(context.getBlockPos()).getBlock();
		Optional<InWorldRecipe> matching = InWorldRecipeRegistries.findMatchingRecipe(InWorldRecipeRegistries.SWORD,
				context, block);
		if (matching.isEmpty())
			return ActionResult.FAIL;
		if (!world.isClient) {
			matching.get().action().accept(context);
			PlayerEntity playerEntity = context.getPlayer();
			if (playerEntity != null) {
				context.getStack().damage(1, playerEntity, (p) -> p.sendToolBreakStatus(context.getHand()));
			}
		}
		return ActionResult.success(world.isClient);
	}
}
