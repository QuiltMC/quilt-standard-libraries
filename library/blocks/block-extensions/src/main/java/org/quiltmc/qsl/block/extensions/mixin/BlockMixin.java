package org.quiltmc.qsl.block.extensions.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.quiltmc.qsl.block.extensions.impl.QuiltBlockInternals;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;

@Mixin(Block.class)
public abstract class BlockMixin {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void computeExtraData(AbstractBlock.Settings settings, CallbackInfo ci) {
		QuiltBlockInternals.computeExtraData((Block) (Object) this);
	}
}
