package org.quiltmc.qsl.registry.mixin.compat;

import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.datafixer.Schemas;

import org.quiltmc.qsl.registry.impl.datafixer.fix.QuiltStatusEffectIdFix;

@Mixin(Schemas.class)
public class SchemasMixin {
	@Inject(
		method = "build",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lcom/mojang/datafixers/DataFixerBuilder;addFixer(Lcom/mojang/datafixers/DataFix;)V",
			ordinal = 0
		),
		slice = @Slice(
			from = @At(value = "CONSTANT", args = "intValue=3568")
		),
		locals = LocalCapture.CAPTURE_FAILHARD
	)
	private static void injectQuiltStatusEffectIdFix(DataFixerBuilder builder, CallbackInfo ci, Schema schema192) {
		builder.addFixer(new QuiltStatusEffectIdFix(schema192));
	}
}
