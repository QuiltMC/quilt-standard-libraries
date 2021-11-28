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

package org.quiltmc.qsl.tag.impl.client;

import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.tag.api.TagType;
import org.quiltmc.qsl.tag.impl.TagDelegate;

@ApiStatus.Internal
final class ClientTagDelegate<T> extends TagDelegate<T> {
	private final Supplier<TagGroup<T>> defaultGroupSupplier;

	public ClientTagDelegate(Identifier id, TagType type, Supplier<TagGroup<T>> tagGroupSupplier, Supplier<TagGroup<T>> defaultGroupSupplier) {
		super(id, type, tagGroupSupplier);
		this.defaultGroupSupplier = defaultGroupSupplier;
	}

	@Override
	protected Tag<T> getTag() {
		var target = this.target;
		boolean needsCompute = target == null;
		TagGroup<T> currentGroup = this.tagGroupSupplier.get();
		Tag<T> tag;

		// Either the tag is entirely missing, or the computed tag is part of a group that isn't the same as
		// the current server-provided group.
		if (needsCompute || target.group() != currentGroup) {
			// Gets the tag from the server-provided group.
			tag = currentGroup.getTag(this.getId());

			// If the tag is absent from the server, let's try to set a default.
			if (tag == null) {
				TagGroup<T> defaultGroup = this.defaultGroupSupplier.get();

				// Either the tag is entirely missing, or the computed tag is part of a group that isn't the same as
				// the current client-provided default group.
				if (needsCompute || target.group() != defaultGroup) {
					tag = defaultGroup.getTagOrEmpty(this.getId());
					this.target = new Target<>(defaultGroup, tag);
				} else {
					tag = target.tag();
				}
			} else { // The tag is present on the server, use it.
				this.target = new Target<>(currentGroup, tag);
			}
		} else {
			tag = target.tag();
		}

		return tag;
	}
}
