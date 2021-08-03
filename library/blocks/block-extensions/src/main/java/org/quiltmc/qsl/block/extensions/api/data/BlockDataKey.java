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

package org.quiltmc.qsl.block.extensions.api.data;

/**
 * Represents a key that can be used to get a value from an {@link ExtraBlockData} instance.
 *
 * @param <T> value type
 */
public record BlockDataKey<T>(Class<T> type, String name) {
	/**
	 * Creates a new key.
	 *
	 * @param type value type
	 * @param name name
	 */
	public BlockDataKey { }

	/**
	 * Gets the value type of this key.
	 *
	 * @return value type
	 */
	@Override
	public Class<T> type() {
		return type;
	}

	/**
	 * Gets the name of this key.
	 *
	 * @return name
	 */
	@Override
	public String name() {
		return name;
	}
}
