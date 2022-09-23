/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.block.content.registry.api;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Block;
import net.minecraft.util.registry.Registry;

public record ReversibleBlockEntry(Block block, boolean reversible) {
	public static final Codec<ReversibleBlockEntry> CODEC = Codec.either(
					RecordCodecBuilder.<ReversibleBlockEntry>create(instance ->
							instance.group(
									Registry.BLOCK.getCodec().fieldOf("block").forGetter(ReversibleBlockEntry::block),
									Codec.BOOL.fieldOf("reversible").forGetter(ReversibleBlockEntry::reversible)
							).apply(instance, ReversibleBlockEntry::new)),
					Registry.BLOCK.getCodec())
			.xmap(
					either -> either.map(entry -> entry, b -> new ReversibleBlockEntry(b, true)),
					entry -> entry.reversible ? Either.right(entry.block) : Either.left(entry));
}
