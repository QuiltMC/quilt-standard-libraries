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

package org.quiltmc.qsl.registry.attribute.impl.reloader;

import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import java.util.*;

sealed interface AttributeTarget {
	Collection<Identifier> ids();

	final class Single implements AttributeTarget {
		private final List<Identifier> ids;

		public Single(Identifier id) {
			ids = Collections.singletonList(id);
		}

		@Override
		public Collection<Identifier> ids() {
			return ids;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Single that = (Single) o;
			return ids.get(0).equals(that.ids.get(0));
		}

		@Override
		public int hashCode() {
			return Objects.hash(ids.get(0));
		}
	}

	// FIXME seal this up when you figure out what tf to do here
	non-sealed abstract class Tagged<T> implements AttributeTarget {
		private final Registry<T> registry;
		private final Identifier tagId;

		public Tagged(Registry<T> registry, Identifier tagId) {
			this.registry = registry;
			this.tagId = tagId;
		}

		protected abstract Tag.Identified<T> getTag(RegistryKey<? extends Registry<T>> registryKey, Identifier tagId);

		@Override
		public Collection<Identifier> ids() {
			Tag.Identified<T> tag = getTag(registry.getKey(), tagId);
			Set<Identifier> ids = new HashSet<>();
			for (T value : tag.values()) {
				ids.add(registry.getId(value));
			}
			return ids;
		}

		@Override
		public abstract boolean equals(Object o);
		@Override
		public abstract int hashCode();
	}
}
