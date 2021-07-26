package org.quiltmc.qsl.inworldrecipes.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.quiltmc.qsl.inworldrecipes.impl.InWorldRecipeMaps;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PickaxeItem.class)
public abstract class PickaxeItemMixin extends Item {
	public PickaxeItemMixin() {
		super(new Settings());
		throw new AssertionError();
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		if (!InWorldRecipeMaps.tryPerform(InWorldRecipeMaps.pickaxe, world.getBlockState(context.getBlockPos()).getBlock(), context))
			return ActionResult.FAIL;
		if (!world.isClient) {
			PlayerEntity playerEntity = context.getPlayer();
			if (playerEntity != null) {
				context.getStack().damage(1, playerEntity, (p) -> p.sendToolBreakStatus(context.getHand()));
			}
		}
		return ActionResult.success(world.isClient);
	}
}
