/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.base.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class QuiltBaseImpl {
	public static final Logger LOGGER = LogManager.getLogger("quilt_base");
	/**
	 * Represents the number of ticks before an auto test server audits mixins and shutdowns.
	 * <p>
	 * Set with {@code -Dquilt.auto_test}.
	 */
	public static final Integer AUTO_TEST_SERVER_TICK_TIME;

	private QuiltBaseImpl() {
		throw new UnsupportedOperationException("QuiltBaseImpl only contains static definitions.");
	}

	static {
		String autoTest = System.getProperty("quilt.auto_test");
		Integer autoTestTickTime = null;

		if (autoTest != null) {
			try {
				autoTestTickTime = Integer.parseInt(autoTest);
			} catch (NumberFormatException e) {
				autoTestTickTime = 50;
			}
		}

		AUTO_TEST_SERVER_TICK_TIME = autoTestTickTime;
	}
}
