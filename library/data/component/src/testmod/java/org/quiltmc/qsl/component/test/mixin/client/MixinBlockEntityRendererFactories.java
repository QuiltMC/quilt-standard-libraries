package org.quiltmc.qsl.component.test.mixin.client;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import org.quiltmc.qsl.component.impl.util.ErrorUtil;
import org.quiltmc.qsl.component.test.ComponentTestMod;
import org.quiltmc.qsl.component.test.client.TestBeRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRendererFactories.class)
public abstract class MixinBlockEntityRendererFactories {
	@Shadow
	protected static <T extends BlockEntity> void register(BlockEntityType<? extends T> type, BlockEntityRendererFactory<T> factory) {
		throw ErrorUtil.illegalState("This should never happen").get();
	}

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void registerCustom(CallbackInfo ci) {
		register(ComponentTestMod.TEST_BE_TYPE, context -> new TestBeRenderer());
	}
}
