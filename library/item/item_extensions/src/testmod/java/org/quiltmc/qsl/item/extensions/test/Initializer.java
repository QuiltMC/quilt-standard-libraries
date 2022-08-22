package org.quiltmc.qsl.item.extensions.test;

import org.jetbrains.annotations.NotNull;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.item.extensions.api.event.ItemInteractionEvents;

public final class Initializer implements ModInitializer,
		ItemInteractionEvents.IgniteBlock {
	public static final Initializer INSTANCE = new Initializer();

	private Initializer() {}

	@Override
	public void onInitialize(ModContainer mod) {}

	@Override
	public @NotNull ActionResult onIgniteBlock(@NotNull ItemUsageContext context) {
		if (context.getBlockState().isOf(Blocks.IRON_BLOCK)) {
			context.playSoundAtBlock(SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS,
					1.0F, context.getWorldRandom().nextFloat() * 0.4F + 0.8F);
			context.replaceBlock(Blocks.NETHERITE_BLOCK.getDefaultState());
			context.damageStack();
			return context.success();
		}
		return ActionResult.PASS;
	}
}
