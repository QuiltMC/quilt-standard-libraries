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

package org.quiltmc.qsl.tag.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.tag.Tag;

import org.quiltmc.qsl.tag.api.QuiltTag;
import org.quiltmc.qsl.tag.api.TagType;

@Mixin(Tag.class)
public interface TagMixin<T> extends QuiltTag<T> {
	@Override
	default TagType getType() {
		return TagType.OPTIONAL;
	}

	@Override
	default boolean hasBeenReplaced() {
		return false; // The goal is to prevent hard-fails with custom Tag implementations.
		// Please look at the javadoc of the method for more insight.
	}
}
