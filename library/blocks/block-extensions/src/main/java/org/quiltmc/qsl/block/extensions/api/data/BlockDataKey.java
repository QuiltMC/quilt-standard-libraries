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

import net.minecraft.util.Identifier;
import java.util.Objects;

/**
 * Represents a key that can be used to get a value from an {@link ExtraBlockData} instance.
 *
 */
@SuppressWarnings("ClassCanBeRecord") // records and generics don't work well together, apparently
public final class BlockDataKey<T> {
	private final Class<T> type;
	private final Identifier name;

	/**
	 * Creates a new key.
	 *
	 * @param type value type
	 * @param name name
	 */
	public BlockDataKey(Class<T> type, Identifier name) {
		this.type = type;
		this.name = name;
	}

	/**
	 * Gets the value type of this key.
	 *
	 * @return value type
	 */
	public Class<T> type() {
		return type;
	}

	/**
	 * Gets the name of this key.
	 *
	 * @return name
	 */
	public Identifier name() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (BlockDataKey<?>) obj;
		return Objects.equals(this.type, that.type) &&
				Objects.equals(this.name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, name);
	}

	@Override
	public String toString() {
		return "BlockDataKey[" +
				"type=" + type + ", " +
				"name=" + name + ']';
	}

}
