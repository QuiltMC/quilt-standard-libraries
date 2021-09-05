package org.quiltmc.qsl.crash.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.crash.CrashReportSection;
import org.quiltmc.qsl.crash.api.CrashReportEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {
	@Inject(method = "populateCrashReport", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/crash/CrashReportSection;add(Ljava/lang/String;Lnet/minecraft/util/crash/CrashCallable;)Lnet/minecraft/util/crash/CrashReportSection;"), locals = LocalCapture.CAPTURE_FAILHARD)
	void addCrashReportDetails(CrashReportSection section, CallbackInfo ci) {
		CrashReportEvents.BLOCKENTITY_DETAILS.invoker().addDetails((BlockEntity) (Object) this, section);
	}
}
