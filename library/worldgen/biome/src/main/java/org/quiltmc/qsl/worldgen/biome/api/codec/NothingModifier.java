/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.worldgen.biome.api.codec;

import com.mojang.serialization.Codec;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.worldgen.biome.api.BiomeModificationContext;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifier;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectionContext;

/**
 * A biome modifier that does nothing; useful to override existing biome modifiers with a datapack.
 * <p>
 * The biome modifier identifier is {@code quilt:nothing}.
 * <p>
 * Example:
 * <pre><code>{
 *     "type": "quilt:nothing"
 * }</code></pre>
 */
public final class NothingModifier implements BiomeModifier {
	public static final Identifier CODEC_ID = new Identifier("quilt", "nothing");
	public static final NothingModifier INSTANCE = new NothingModifier();
	public static final Codec<NothingModifier> CODEC = Codec.unit(INSTANCE);

	private NothingModifier() {}

	@Override
	public Identifier getCodecId() {
		return CODEC_ID;
	}

	@Override
	public boolean shouldModify(BiomeSelectionContext context) {
		return false;
	}

	@Override
	public void modify(BiomeSelectionContext selectionContext, BiomeModificationContext modificationContext) {}
}
