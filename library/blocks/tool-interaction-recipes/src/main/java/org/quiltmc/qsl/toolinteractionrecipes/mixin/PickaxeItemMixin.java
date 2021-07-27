package org.quiltmc.qsl.toolinteractionrecipes.mixin;

import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.toolinteractionrecipes.impl.ToolInteractionRecipeMaps;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PickaxeItem.class)
public abstract class PickaxeItemMixin extends ItemMixin {
	@Override
	public @NotNull ActionResult qsl$useOnBlock(@NotNull ItemUsageContext context) {
		World world = context.getWorld();
		if (!ToolInteractionRecipeMaps.tryPerform(ToolInteractionRecipeMaps.pickaxe, world.getBlockState(context.getBlockPos()).getBlock(), context))
			return ActionResult.FAIL;
		return ActionResult.success(world.isClient);
	}
}
