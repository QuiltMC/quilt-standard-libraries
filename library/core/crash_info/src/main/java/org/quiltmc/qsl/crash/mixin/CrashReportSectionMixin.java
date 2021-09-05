package org.quiltmc.qsl.crash.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.HeightLimitView;
import org.quiltmc.qsl.crash.api.CrashReportEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CrashReportSection.class)
public abstract class CrashReportSectionMixin {
	@Inject(method = "addBlockInfo", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
	private static void addCrashReportDetails(CrashReportSection element, HeightLimitView world, BlockPos pos, BlockState state, CallbackInfo ci) {
		CrashReportEvents.BLOCK_DETAILS.invoker().addDetails(world, pos, state, element);
	}
}
