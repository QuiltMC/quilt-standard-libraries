package org.quiltmc.qsl.registry.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.BuiltinRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BuiltinRegistries.class)
public class BuiltinRegistriesMixin {

	@Inject(method = "freeze", at = @At("RETURN"))
	private static void onFreezeBuiltins(CallbackInfo ci) {
		//region Fix MC-197259
		final List<BlockState> states = BuiltinRegistries.BLOCK.stream()
				.flatMap(block -> block.getStateManager().getStates().stream())
				.toList();

		final int xLength = MathHelper.ceil(MathHelper.sqrt(states.size()));
		final int zLength = MathHelper.ceil(states.size() / (float) xLength);

		DebugChunkGeneratorAccessor.setBlockStates(states);
		DebugChunkGeneratorAccessor.setXSideLength(xLength);
		DebugChunkGeneratorAccessor.setZSideLength(zLength);
		//endregion
	}
}
