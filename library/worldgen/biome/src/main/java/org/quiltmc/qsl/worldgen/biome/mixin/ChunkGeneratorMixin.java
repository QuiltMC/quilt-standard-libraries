package org.quiltmc.qsl.worldgen.biome.mixin;

import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.class_7138;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import org.quiltmc.qsl.worldgen.biome.impl.TheEndBiomeData;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {
	/**
	 * Injection into {@link ChunkGenerator#populateBiomes(Registry, Executor, class_7138, Blender, StructureManager, Chunk)},
	 * first lambda.
	 */
	@Inject(method = "method_38267", at = @At("HEAD"))
	private void populateBiomes(Chunk chunk, class_7138 noiseConfig, CallbackInfoReturnable<Chunk> ci) {
		// capture seed so TheEndBiomeData.Overrides has it if it needs it
		TheEndBiomeData.Overrides.setSeed(noiseConfig.method_42369());
	}
}
