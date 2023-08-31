/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.registry.impl.sync;

import java.util.stream.IntStream;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;

public class ProtocolVersions {
	public static final int CURRENT_VERSION = 3;
	public static final int OLDEST_SUPPORTED_VERSION = 3;
	public static final IntSet IMPL_SUPPORTED_VERSIONS = IntSet.of(IntStream.rangeClosed(OLDEST_SUPPORTED_VERSION, CURRENT_VERSION).toArray());

	public static final int NO_PROTOCOL = -1;
	public static final int FAPI_PROTOCOL = -2;

	public static int getHighestSupportedLocal(IntList supportedRemote) {
		return getHighestSupported(IMPL_SUPPORTED_VERSIONS, supportedRemote);
	}

	public static int getHighestSupported(IntCollection supportedLocal, IntList supportedRemote) {
		int highestSupported = NO_PROTOCOL;

		for (var i = 0; i < supportedRemote.size(); i++) {
			int version = supportedRemote.getInt(i);

			if (version > highestSupported && supportedLocal.contains(version)) {
				highestSupported = version;
			}
		}

		return highestSupported;
	}
}
