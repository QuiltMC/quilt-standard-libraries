/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.crash.test;

import java.util.Random;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.crash.api.CrashReportEvents;

/**
 * Test mod for Crash Report Events.
 *
 * <p>To cause an entity crash, summon an end crystal atop a block of diamond.
 *
 * <p>To cause a BE crash, place a furnace atop a block of diamond.
 */
public class CrashReportApiTestMod implements ModInitializer {
	@Override
	public void onInitialize(ModContainer mod) {
		CrashReportEvents.SYSTEM_DETAILS.register(details -> {
			details.addSection("Value of Pi", Double.toString(Math.PI));
		});

		CrashReportEvents.WORLD_DETAILS.register((world, section) -> {
			section.add("Biome at 0,0,0", world.getRegistryManager().get(RegistryKeys.BIOME).getId(world.getBiome(BlockPos.ORIGIN).value()));
		});

		CrashReportEvents.BLOCK_DETAILS.register((world, pos, state, section) -> {
			section.add("World height", world.getHeight());
		});

		CrashReportEvents.BLOCKENTITY_DETAILS.register((entity, section) -> {
			section.add("Is removed", entity.isRemoved());
		});

		CrashReportEvents.ENTITY_DETAILS.register((entity, section) -> {
			section.add("Entity age", entity.age);
		});

		CrashReportEvents.CRASH_REPORT_CREATION.register(report -> {
			report.addElement("Test Section", 2)
					.add("A thing?", "A thing.")
					.add("A random number", new Random().nextInt());
		});
	}
}
