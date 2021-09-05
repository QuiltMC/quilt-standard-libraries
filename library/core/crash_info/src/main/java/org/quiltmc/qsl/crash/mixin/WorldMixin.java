package org.quiltmc.qsl.crash.mixin;

import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.world.World;
import org.quiltmc.qsl.crash.api.CrashReportEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(World.class)
public abstract class WorldMixin {
	@Inject(method = "addDetailsToCrashReport", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
	void addCrashReportDetails(CrashReport report, CallbackInfoReturnable<CrashReportSection> cir, CrashReportSection crashReportSection) {
		CrashReportEvents.WORLD_DETAILS.invoker().addDetails((World) (Object) this, crashReportSection);
	}
}
