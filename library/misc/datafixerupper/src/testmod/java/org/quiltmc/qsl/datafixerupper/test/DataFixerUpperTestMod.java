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

package org.quiltmc.qsl.datafixerupper.test;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.datafixerupper.api.QuiltDataFixerBuilder;
import org.quiltmc.qsl.datafixerupper.api.QuiltDataFixes;

public final class DataFixerUpperTestMod implements ModInitializer {
	private static final String NAMESPACE = "quilt_datafixerupper_testmod";
	private static final int DATA_VERSION = 0;

	private static final Logger LOGGER = LogUtils.getLogger();

	private static final Item ITEM = Registry.register(
			Registry.ITEM, new Identifier(NAMESPACE, "old_item"), new Item(new Item.Settings()));

	@Override
	public void onInitialize(ModContainer mod) {
		QuiltDataFixerBuilder builder = new QuiltDataFixerBuilder(DATA_VERSION);
		builder.addSchema(0, QuiltDataFixes.MOD_SCHEMA);

		QuiltDataFixes.registerFixer(NAMESPACE, DATA_VERSION, builder.build(Util::getBootstrapExecutor));
	}
}
