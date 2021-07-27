package org.quiltmc.qsl.toolinteractionrecipes.mixin;

import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.SwordItem;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.toolinteractionrecipes.impl.ToolInteractionRecipeMaps;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SwordItem.class)
public abstract class SwordItemMixin extends ItemMixin {
	@Override
	public @NotNull ActionResult qsl$useOnBlock(@NotNull ItemUsageContext context) {
		World world = context.getWorld();
		if (!ToolInteractionRecipeMaps.tryPerform(ToolInteractionRecipeMaps.sword, world.getBlockState(context.getBlockPos()).getBlock(), context))
			return ActionResult.FAIL;
		return ActionResult.success(world.isClient);
	}
}
