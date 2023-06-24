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

package org.quiltmc.qsl.base.test;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.base.test.event.EventListenAllTests;
import org.quiltmc.qsl.base.test.event.EventTests;

public final class QuiltBaseTestMod implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(QuiltBaseTestMod.class);

	@Override
	public void onInitialize(ModContainer mod) {
		List.of(new EventTests(), new EventListenAllTests())
				.forEach(test -> {
					LOGGER.info("Testing " + test.getClass().getSimpleName() + "...");
					test.run();
				});
	}
}
