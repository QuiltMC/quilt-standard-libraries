package org.quiltmc.qsl.inworldrecipes.mixin;

import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.inworldrecipes.impl.InWorldRecipeMaps;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PickaxeItem.class)
public abstract class PickaxeItemMixin extends ItemMixin {
	@Override
	public @NotNull ActionResult qsl$useOnBlock(@NotNull ItemUsageContext context) {
		World world = context.getWorld();
		if (!InWorldRecipeMaps.tryPerform(InWorldRecipeMaps.pickaxe, world.getBlockState(context.getBlockPos()).getBlock(), context))
			return ActionResult.FAIL;
		return ActionResult.success(world.isClient);
	}
}
