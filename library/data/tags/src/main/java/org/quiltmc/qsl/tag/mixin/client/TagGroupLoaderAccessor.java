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

package org.quiltmc.qsl.tag.mixin.client;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import com.google.common.collect.Multimap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroupLoader;
import net.minecraft.util.Identifier;

@Mixin(TagGroupLoader.class)
public interface TagGroupLoaderAccessor {
	@Invoker("method_32839")
	static void quilt$visitDependenciesAndElement(Map<Identifier, Tag.Builder> map,
	                                            Multimap<Identifier, Identifier> tagEntries, Set<Identifier> set,
	                                            Identifier identifier, BiConsumer<Identifier, Tag.Builder> consumer) {
		throw new IllegalStateException("Invoker injection failed.");
	}

	@Invoker("method_32844")
	static void quilt$addDependencyIfNotCyclic(Multimap<Identifier, Identifier> tagEntries, Identifier tagId, Identifier entryId) {
		throw new IllegalStateException("Invoker injection failed.");
	}
}
