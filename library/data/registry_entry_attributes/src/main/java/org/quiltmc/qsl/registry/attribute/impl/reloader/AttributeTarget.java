package org.quiltmc.qsl.registry.attribute.impl.reloader;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import java.util.*;

sealed interface AttributeTarget permits AttributeTarget.Single, AttributeTarget.Tag {
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
	non-sealed abstract class Tag<T> implements AttributeTarget {
		private final Registry<T> registry;
		private final Identifier tagId;

		public Tag(Registry<T> registry, Identifier tagId) {
			this.registry = registry;
			this.tagId = tagId;
		}

		protected abstract net.minecraft.tag.Tag.Identified<T> getTag(RegistryKey<? extends Registry<T>> registryKey, Identifier tagId);

		@Override
		public Collection<Identifier> ids() {
			net.minecraft.tag.Tag.Identified<T> tag = getTag(registry.getKey(), tagId);
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
