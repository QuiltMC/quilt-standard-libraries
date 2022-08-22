package org.quiltmc.qsl.entity.custom_spawn_data.test.mixin;

import com.google.common.collect.ImmutableMap.Builder;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModels;
import org.quiltmc.qsl.entity.custom_spawn_data.test.BoxModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(EntityModels.class)
public class EntityModelsMixin {
	@Inject(
			method = "getModels",
			at = @At(
					value = "INVOKE",
					target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;",
					shift = Shift.BEFORE,
					remap = false
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private static void quilt$addBoxModelLayer(CallbackInfoReturnable<Map<EntityModelLayer, TexturedModelData>> cir,
											   Builder<EntityModelLayer, TexturedModelData> builder) {
		builder.put(BoxModel.LAYER, BoxModel.getTexturedModelData());
	}
}
