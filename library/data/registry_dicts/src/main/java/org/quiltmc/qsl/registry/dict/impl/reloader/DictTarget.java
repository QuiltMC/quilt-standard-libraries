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

package org.quiltmc.qsl.registry.dict.impl.reloader;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

sealed interface DictTarget {
	Collection<Identifier> ids() throws ResolveException;

	final class ResolveException extends Exception {
		public ResolveException(String message) {
			super(message);
		}
	}

	final class Single implements DictTarget {
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

	@SuppressWarnings("ClassCanBeRecord")
	final class Tagged<T> implements DictTarget {
		private final TagGetter tagGetter;
		private final Registry<T> registry;
		private final Identifier tagId;
		private final boolean required;

		public Tagged(TagGetter tagGetter, Registry<T> registry, Identifier tagId, boolean required) {
			this.tagGetter = tagGetter;
			this.registry = registry;
			this.tagId = tagId;
			this.required = required;
		}

		@Override
		public Collection<Identifier> ids() throws ResolveException {
			Tag<T> tag = tagGetter.getTag(registry.getKey(), tagId, required);
			if (tag == null) {
				if (required) {
					throw new ResolveException("Tag " + tagId + " does not exist!");
				} else {
					return Set.of();
				}
			}
			Set<Identifier> ids = new HashSet<>();
			for (T value : tag.values()) {
				ids.add(registry.getId(value));
			}
			return ids;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Tagged<?> tagged = (Tagged<?>) o;
			return registry.getKey().equals(tagged.registry.getKey()) && tagId.equals(tagged.tagId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(registry.getKey(), tagId);
		}
	}
}
