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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Range;

public record FlammableBlockEntry(@Range(from = 0, to = Integer.MAX_VALUE) int burn, @Range(from = 0, to = Integer.MAX_VALUE) int spread) {
	public static final Codec<FlammableBlockEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.intRange(0, Integer.MAX_VALUE).fieldOf("burn").forGetter(FlammableBlockEntry::burn),
			Codec.intRange(0, Integer.MAX_VALUE).fieldOf("spread").forGetter(FlammableBlockEntry::spread)
	).apply(instance, FlammableBlockEntry::new));
}
