/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.tag.impl;

import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.tag.api.QuiltTag;
import org.quiltmc.qsl.tag.api.TagType;

/**
 * Represents a delegated tag. A delegated tag is a wrapper around a tag which is retrieved at runtime,
 * and is kept up to date throughout the lifecycle of the game.
 * <p>
 * Thread safety is being ensured by using an immutable holder object for consistently retrieving both result
 * and condition, volatile for safe publishing and assuming {@link TagGroup#getTagOrEmpty(Identifier)}
 * is safe to call concurrently.
 * <p>
 * It should be possible to exploit a benign data race on {@link #target} by removing volatile, but this option
 * hasn't been chosen yet since a performance problem in the area is yet to be proven.
 * <p>
 * This class is for internal use only, this should not directly be used outside this module.
 */
@ApiStatus.Internal
public class TagDelegate<T> implements Tag.Identified<T>, QuiltTag<T>, QuiltTagHooks {
	private final Identifier id;
	private final TagType type;
	protected final Supplier<TagGroup<T>> tagGroupSupplier;
	protected volatile Target<T> target;
	private int replacementCount = 0;

	public TagDelegate(Identifier id, TagType type, Supplier<TagGroup<T>> tagGroupSupplier) {
		this.id = id;
		this.type = type;
		this.tagGroupSupplier = tagGroupSupplier;
	}

	@Override
	public Identifier getId() {
		return this.id;
	}

	@Override
	public boolean contains(T entry) {
		return this.getTag().contains(entry);
	}

	@Override
	public List<T> values() {
		return this.getTag().values();
	}

	@Override
	public TagType getType() {
		return this.type;
	}

	@Override
	public boolean hasBeenReplaced() {
		return this.replacementCount > 0;
	}

	/**
	 * Retrieves the tag this delegate is pointing to, computing it if missing or outdated.
	 * <p>
	 * Thread safety is being ensured by using an immutable holder object for consistently retrieving both result
	 * and condition, volatile for safe publishing and assuming {@link TagGroup#getTagOrEmpty(Identifier)}
	 * is safe to call concurrently.
	 * <p>
	 * It should be possible to exploit a benign data race on {@link #target} by removing volatile, but this option
	 * hasn't been chosen yet since a performance problem in the area is yet to be proven.
	 */
	protected Tag<T> getTag() {
		var target = this.target;
		TagGroup<T> currentGroup = this.tagGroupSupplier.get();
		Tag<T> tag;

		if (target == null || target.group() != currentGroup) {
			tag = currentGroup.getTagOrEmpty(this.getId());
			this.target = new Target<>(currentGroup, tag);
		} else {
			tag = target.tag();
		}

		return tag;
	}

	@Override
	public void quilt$setReplacementCount(int replacementCount) {
		this.replacementCount = replacementCount;
	}

	@Override
	public String toString() {
		return "TagDelegate{id=\"" + this.id
				+ "\", type=" + this.type
				+ ", target=" + this.target
				+ ", replacementCount=" + this.replacementCount
				+ ", values=" + this.values()
				+ "}";
	}

	public static record Target<T>(TagGroup<T> group, Tag<T> tag) {
	}
}
