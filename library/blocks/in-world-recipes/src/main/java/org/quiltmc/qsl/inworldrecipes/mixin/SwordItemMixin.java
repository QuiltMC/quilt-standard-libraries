package org.quiltmc.qsl.inworldrecipes.mixin;

import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.SwordItem;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.inworldrecipes.impl.InWorldRecipeMaps;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SwordItem.class)
public abstract class SwordItemMixin extends ItemMixin {
	@Override
	public @NotNull ActionResult qsl$useOnBlock(@NotNull ItemUsageContext context) {
		World world = context.getWorld();
		if (!InWorldRecipeMaps.tryPerform(InWorldRecipeMaps.sword, world.getBlockState(context.getBlockPos()).getBlock(), context))
			return ActionResult.FAIL;
		return ActionResult.success(world.isClient);
	}
}
