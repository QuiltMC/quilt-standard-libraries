package org.quiltmc.qsl.crash.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.util.crash.CrashReportSection;
import org.quiltmc.qsl.crash.api.CrashReportEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Inject(method = "populateCrashReport", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
	void addCrashReportDetails(CrashReportSection section, CallbackInfo ci) {
		CrashReportEvents.ENTITY_DETAILS.invoker().addDetails((Entity) (Object) this, section);
	}
}
