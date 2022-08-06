/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2022 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.worldgen.dimension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryOps;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.RandomState;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.structure.StructureSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmptyChunkGenerator extends ChunkGenerator {
	private static final Logger EMPTY_CHUNK_GENERATOR_LOGGER = LoggerFactory.getLogger("QuiltDimensionTest|EmptyChunkGenerator");
	public static final Codec<EmptyChunkGenerator> CODEC = RecordCodecBuilder.create((instance) -> method_41042(instance).and(RegistryOps.getRegistry(Registry.BIOME_KEY).forGetter((generator) -> generator.biomeRegistry)).apply(instance, instance.stable(EmptyChunkGenerator::new)));

	private final Registry<Biome> biomeRegistry;

	public EmptyChunkGenerator(Registry<StructureSet> registry, Registry<Biome> biomeRegistry) {
		super(registry, Optional.empty(), new FixedBiomeSource(biomeRegistry.getOrCreateHolder(BiomeKeys.PLAINS).getOrThrow(false, EMPTY_CHUNK_GENERATOR_LOGGER::error)));
		this.biomeRegistry = biomeRegistry;
	}

	@Override
	protected Codec<? extends ChunkGenerator> getCodec() {
		return CODEC;
	}

	@Override
	public void carve(ChunkRegion chunkRegion, long seed, RandomState randomState, BiomeAccess biomeAccess, StructureManager structureManager, Chunk chunk, GenerationStep.Carver generationStep) {
	}

	@Override
	public void buildSurface(ChunkRegion region, StructureManager structureManager, RandomState randomState, Chunk chunk) {
	}

	@Override
	public void populateEntities(ChunkRegion region) {
	}

	@Override
	public int getWorldHeight() {
		return 256;
	}

	@Override
	public CompletableFuture<Chunk> populateNoise(Executor executor, Blender blender, RandomState randomState, StructureManager structureManager, Chunk chunk) {
		return CompletableFuture.completedFuture(chunk);
	}

	@Override
	public int getSeaLevel() {
		return 0;
	}

	@Override
	public int getMinimumY() {
		return 0;
	}

	@Override
	public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, RandomState randomState) {
		return 0;
	}

	@Override
	public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, RandomState randomState) {
		return new VerticalBlockSample(0, new BlockState[0]);
	}

	@Override
	public void m_hfetlfug(List<String> list, RandomState randomState, BlockPos blockPos) {
	}
}
