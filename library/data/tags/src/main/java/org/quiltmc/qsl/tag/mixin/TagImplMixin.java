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

package org.quiltmc.qsl.tag.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.tag.RequiredTagList;
import net.minecraft.tag.SetTag;
import net.minecraft.tag.Tag;

import org.quiltmc.qsl.tag.api.QuiltTag;
import org.quiltmc.qsl.tag.impl.QuiltTagHooks;

@Mixin(value = {RequiredTagList.TagWrapper.class, SetTag.class})
public abstract class TagImplMixin<T> implements QuiltTag<T>, QuiltTagHooks, Tag<T> {
	@Unique
	private int quilt$replaced;

	@Override
	public boolean hasBeenReplaced() {
		return this.quilt$replaced > 0;
	}

	@Override
	public void quilt$setReplacementCount(int replacementCount) {
		this.quilt$replaced = replacementCount;
	}
}
