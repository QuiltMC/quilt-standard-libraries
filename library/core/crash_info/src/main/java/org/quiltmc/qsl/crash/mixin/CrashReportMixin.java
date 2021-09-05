package org.quiltmc.qsl.crash.mixin;

import net.minecraft.util.crash.CrashReport;
import org.quiltmc.qsl.crash.api.CrashReportEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrashReport.class)
public abstract class CrashReportMixin {
	@Inject(method = "addStackTrace", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
	void onCrashReportCreated(StringBuilder crashReportBuilder, CallbackInfo ci) {
		CrashReportEvents.CRASH_REPORT_CREATION.invoker().onCreate((CrashReport) (Object) this);
	}
}
