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

package org.quiltmc.qsl.registry.attachment.impl.reloader;

import java.util.*;

import net.minecraft.tag.Tag;
import net.minecraft.util.Holder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.tag.api.QuiltTagKey;
import org.quiltmc.qsl.tag.api.TagRegistry;
import org.quiltmc.qsl.tag.api.TagType;

interface ValueTarget {
	Collection<Identifier> ids() throws ResolveException;

	final class ResolveException extends Exception {
		public ResolveException(String message) {
			super(message);
		}
	}

	final class Single implements ValueTarget {
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
	final class Tagged<T> implements ValueTarget {
		private final Registry<T> registry;
		private final Identifier id;
		private final boolean isClient;
		private final boolean required;

		public Tagged(Registry<T> registry, Identifier id, boolean isClient, boolean required) {
			this.registry = registry;
			this.id = id;
			this.isClient = isClient;
			this.required = required;
		}

		@Override
		public Collection<Identifier> ids() throws ResolveException {
			var entry = TagRegistry.stream(registry.getKey())
					.filter(entry1 -> id.equals(entry1.key().id()))
					.findFirst();

			if (entry.isEmpty()) {
				if (required) {
					throw new ResolveException("Tag " + id + " does not exist!");
				} else {
					return Set.of();
				}
			}

			if (!isClient) {
				@SuppressWarnings("unchecked") var type = ((QuiltTagKey<T>) (Object) entry.get().key()).type();
				if (type == TagType.CLIENT_FALLBACK || type == TagType.CLIENT_ONLY) {
					throw new ResolveException("Tag " + id + " is client-only, but this attachment is not!");
				}
			}

			return collectTagEntries(registry, entry.get().tag(), new HashSet<>());
		}

		private static <R> Set<Identifier> collectTagEntries(Registry<R> registry, Tag<Holder<R>> tag, Set<Identifier> ids) {
			for (var value : tag.values()) {
				switch (value.getKind()) {
					case DIRECT -> ids.add(registry.getId(value.value()));
					case REFERENCE -> value.streamTags().forEach(key -> collectTagEntries(registry, TagRegistry.getTag(key), ids));
				}
			}
			return ids;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Tagged<?> tagged = (Tagged<?>) o;
			return registry.getKey().equals(tagged.registry.getKey()) && id.equals(tagged.id);
		}

		@Override
		public int hashCode() {
			return Objects.hash(registry.getKey(), id);
		}
	}
}
