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

import java.util.List;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.tag.RequiredTagList;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.tag.api.QuiltTag;
import org.quiltmc.qsl.tag.api.TagType;
import org.quiltmc.qsl.tag.impl.QuiltRequiredTagListHooks;
import org.quiltmc.qsl.tag.impl.QuiltRequiredTagWrapperHooks;
import org.quiltmc.qsl.tag.impl.TagRegistryImpl;

@Mixin(RequiredTagList.class)
public class RequiredTagListMixin<T> implements QuiltRequiredTagListHooks<T> {
	@Shadow
	@Final
	private List<RequiredTagList.TagWrapper<T>> tags;

	@Override
	public Tag.Identified<T> qsl$addTag(Identifier id, TagType type) {
		var wrapper = new RequiredTagList.TagWrapper<T>(id);
		//noinspection ConstantConditions
		((QuiltRequiredTagWrapperHooks) wrapper).qsl$setTagType(type);
		this.tags.add(wrapper);
		return wrapper;
	}

	@Redirect(method = "getMissingTags", at = @At(value = "INVOKE", target = "Ljava/util/List;stream()Ljava/util/stream/Stream;"))
	private Stream<RequiredTagList.TagWrapper<T>> onTagsStream(List<RequiredTagList.TagWrapper<T>> list) {
		if (TagRegistryImpl.isClientFetchingMissingTags()) {
			return list.stream().filter(wrapper -> QuiltTag.cast(wrapper).getType().isRequiredToConnect());
		} else {
			return list.stream();
		}
	}

	@Mixin(RequiredTagList.TagWrapper.class)
	public abstract static class TagWrapperMixin<T> implements QuiltTag<T>, QuiltRequiredTagWrapperHooks {
		private TagType tagType = TagType.CLIENT_SERVER_REQUIRED;

		@Override
		public TagType getType() {
			return tagType;
		}

		@Override
		public void qsl$setTagType(TagType tagType) {
			this.tagType = tagType;
		}
	}
}
