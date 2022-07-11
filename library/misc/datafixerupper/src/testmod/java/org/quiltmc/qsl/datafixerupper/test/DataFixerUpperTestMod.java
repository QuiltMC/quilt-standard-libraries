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

import com.mojang.datafixers.schemas.Schema;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;
import net.minecraft.util.Util;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.datafixerupper.api.QuiltDataFixerBuilder;
import org.quiltmc.qsl.datafixerupper.api.QuiltDataFixes;
import org.quiltmc.qsl.datafixerupper.api.SimpleFixes;

public final class DataFixerUpperTestMod implements ModInitializer {
	private static final String NAMESPACE = "quilt_datafixerupper_testmod";

	private static final Logger LOGGER = LogUtils.getLogger();

	@Override
	public void onInitialize(ModContainer mod) {
		QuiltDataFixerBuilder builder = new QuiltDataFixerBuilder(1);
		builder.addSchema(0, QuiltDataFixes.MOD_SCHEMA);
		Schema schemaV1 = builder.addSchema(1, IdentifierNormalizingSchema::new);
		SimpleFixes.addItemRenameFix(builder, "Rename old_item to new_item",
				NAMESPACE + ":old_item", NAMESPACE + ":new_item", schemaV1);

		QuiltDataFixes.registerFixer(NAMESPACE, 1, builder.build(Util::getBootstrapExecutor));

		// TODO figure out a way to actually test this fixer
	}
}
