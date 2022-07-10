/*
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

package org.quiltmc.qsl.datafixerupper.impl;

import java.util.Collections;
import java.util.Map;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.datafixer.Schemas;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;

@ApiStatus.Internal
public final class QuiltDataFixesInternals {
	private QuiltDataFixesInternals() { }

	public static final Logger LOGGER = LogUtils.getLogger();

	private static Map<String, DataFixerEntry> modDataFixers = new Object2ReferenceOpenHashMap<>();
	private static Schema modSchema;
	private static boolean frozen = false;

	public static void registerFixer(String modId, int currentVersion, DataFixer dataFixer) {
		modDataFixers.put(modId, new DataFixerEntry(dataFixer, currentVersion));
	}

	public static @Nullable DataFixerEntry getFixerEntry(String modId) {
		return modDataFixers.get(modId);
	}

	@SuppressWarnings("deprecation")
	private static final Schema VANILLA_SCHEMA = Schemas.getFixer()
			.getSchema(SharedConstants.getGameVersion().getWorldVersion());

	public static Schema getModSchema() {
		if (modSchema == null) {
			modSchema = new Schema(0, VANILLA_SCHEMA);
		}

		return modSchema;
	}

	public static NbtCompound updateWithAllFixers(DataFixTypes dataFixTypes, NbtCompound compound) {
		NbtCompound current = compound;

		for (Map.Entry<String, DataFixerEntry> entry : modDataFixers.entrySet()) {
			String currentModId = entry.getKey();
			int modIdCurrentDynamicVersion = getModDataVersion(compound, currentModId);
			DataFixerEntry dataFixerEntry = entry.getValue();

			current = (NbtCompound) dataFixerEntry.dataFixer()
					.update(dataFixTypes.getTypeReference(),
							new Dynamic<>(NbtOps.INSTANCE, current),
							modIdCurrentDynamicVersion, dataFixerEntry.currentVersion)
					.getValue();
		}

		return current;
	}

	public static NbtCompound addModDataVersions(NbtCompound compound) {
		for (Map.Entry<String, DataFixerEntry> entry : modDataFixers.entrySet()) {
			compound.putInt(entry.getKey() + "_DataVersion", entry.getValue().currentVersion);
		}

		return compound;
	}

	public static int getModDataVersion(NbtCompound compound, String modId) {
		return compound.getInt(modId + "_DataVersion");
	}

	public static void freeze() {
		if (!frozen) {
			modDataFixers = Collections.unmodifiableMap(modDataFixers);
		}

		frozen = true;
	}

	public static boolean isFrozen() {
		return frozen;
	}

	public record DataFixerEntry(DataFixer dataFixer, int currentVersion) { }
}
