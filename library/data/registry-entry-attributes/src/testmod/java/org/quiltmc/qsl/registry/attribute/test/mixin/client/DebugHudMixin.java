package org.quiltmc.qsl.registry.attribute.test.mixin.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.quiltmc.qsl.registry.attribute.test.client.ClientAttributeTest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {
	@Inject(method = "getRightText", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 2,
			shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
	public void qsl$addTestAttribute(CallbackInfoReturnable<List<String>> cir, long l, long m, long n, long o,
									 List<String> list, BlockPos blockPos, BlockState blockState) {
		list.add("based: " + ClientAttributeTest.ATTRIBUTE.getValue(blockState.getBlock())
				.map(b -> b ? Formatting.GREEN + "yes" : Formatting.RED + "no")
				.orElse(Formatting.BLUE + "unset"));
		list.add(Formatting.UNDERLINE + "Properties:");
	}
}
