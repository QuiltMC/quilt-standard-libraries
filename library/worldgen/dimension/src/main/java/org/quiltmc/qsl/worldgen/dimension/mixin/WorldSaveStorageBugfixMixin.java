/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.worldgen.dimension.mixin;

import java.util.List;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.storage.WorldSaveStorage;

/**
 * After removing a dimension mod or a dimension data pack, Minecraft may fail to enter
 * the world, because it fails to deserialize the chunk generator of the custom dimensions in file {@code level.dat}
 * This mixin will remove the custom dimensions from the nbt tag, so the deserializer and DFU cannot see custom
 * dimensions and won't cause errors.
 * The custom dimensions will be re-added later.
 * <p>
 * This Mixin changes a vanilla behavior that is deemed as a bug (MC-197860). In vanilla, the custom dimension
 * is not removed after uninstalling the dimension data pack.
 * This makes custom dimensions non-removable. Most players don't want this behavior.
 * With this Mixin, custom dimensions will be removed when its data pack is removed.
 */
@Mixin(WorldSaveStorage.class)
public class WorldSaveStorageBugfixMixin {
	@SuppressWarnings("unchecked")
	@Inject(method = "readGeneratorProperties", at = @At("HEAD"))
	private static <T> void onReadGeneratorProperties(
			Dynamic<T> nbt, DataFixer dataFixer, int version,
			CallbackInfoReturnable<Pair<GeneratorOptions, Lifecycle>> cir
	) {
		NbtElement nbtTag = ((Dynamic<NbtElement>) nbt).getValue();

		NbtCompound worldGenSettings = ((NbtCompound) nbtTag).getCompound("WorldGenSettings");

		quilt$removeNonVanillaDimensionsFromNbt(worldGenSettings);
	}

	@Unique
	private static final List<RegistryKey<DimensionOptions>> BASE_DIMENSIONS = List.of(
			DimensionOptions.OVERWORLD, DimensionOptions.NETHER, DimensionOptions.END
	);

	/**
	 * Removes all non-vanilla dimensions from the tag. The custom dimensions will be re-added later from the data packs.
	 */
	@Unique
	private static void quilt$removeNonVanillaDimensionsFromNbt(NbtCompound worldGenSettings) {
		NbtCompound dimensions = worldGenSettings.getCompound("dimensions");

		if (dimensions.getSize() > BASE_DIMENSIONS.size()) {
			var newDimensions = new NbtCompound();

			for (var dimId : BASE_DIMENSIONS) {
				var strId = dimId.getValue().toString();

				if (dimensions.contains(strId)) {
					newDimensions.put(strId, dimensions.getCompound(strId));
				}
			}

			worldGenSettings.put("dimensions", newDimensions);
		}
	}
}
