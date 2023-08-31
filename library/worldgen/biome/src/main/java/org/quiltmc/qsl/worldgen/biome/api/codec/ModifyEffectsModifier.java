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

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.registry.Holder;
import net.minecraft.sound.BiomeAdditionsSound;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.BiomeParticleConfig;

import org.quiltmc.qsl.data.callback.api.predicate.CodecAwarePredicate;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModificationContext;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifier;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectionContext;

/**
 * A biome modifier that modifies the effects of a biome.
 * <p>
 * The biome modifier identifier is {@code quilt:modify_effects}.
 *
 * @see BiomeModificationContext.EffectsContext
 */
public record ModifyEffectsModifier(
		CodecAwarePredicate<BiomeSelectionContext> selector,
		Optional<Integer> fogColor,
		Optional<Integer> waterColor,
		Optional<Integer> waterFogColor,
		Optional<Integer> skyColor,
		Optional<Integer> foliageColor,
		Optional<Integer> grassColor,
		Optional<BiomeEffects.GrassColorModifier> grassColorModifier,
		Optional<BiomeParticleConfig> particleConfig,
		Optional<Holder<SoundEvent>> ambientSound,
		Optional<BiomeMoodSound> moodSound,
		Optional<BiomeAdditionsSound> additionsSound,
		Optional<MusicSound> music
) implements BiomeModifier {
	public static final Identifier CODEC_ID = new Identifier("quilt", "modify_effects");
	public static final Codec<ModifyEffectsModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BiomeModifier.BIOME_SELECTOR_CODEC.fieldOf("selector").forGetter(ModifyEffectsModifier::selector),
			Codec.INT.optionalFieldOf("fog_color").forGetter(ModifyEffectsModifier::fogColor),
			Codec.INT.optionalFieldOf("water_color").forGetter(ModifyEffectsModifier::waterColor),
			Codec.INT.optionalFieldOf("water_fog_color").forGetter(ModifyEffectsModifier::waterFogColor),
			Codec.INT.optionalFieldOf("sky_color").forGetter(ModifyEffectsModifier::skyColor),
			Codec.INT.optionalFieldOf("foliage_color").forGetter(ModifyEffectsModifier::foliageColor),
			Codec.INT.optionalFieldOf("grass_color").forGetter(ModifyEffectsModifier::grassColor),
			BiomeEffects.GrassColorModifier.CODEC.optionalFieldOf("grass_color_modifier").forGetter(ModifyEffectsModifier::grassColorModifier),
			BiomeParticleConfig.CODEC.optionalFieldOf("particle_config").forGetter(ModifyEffectsModifier::particleConfig),
			SoundEvent.CODEC.optionalFieldOf("ambient_sound").forGetter(ModifyEffectsModifier::ambientSound),
			BiomeMoodSound.CODEC.optionalFieldOf("mood_sound").forGetter(ModifyEffectsModifier::moodSound),
			BiomeAdditionsSound.CODEC.optionalFieldOf("additions_sound").forGetter(ModifyEffectsModifier::additionsSound),
			MusicSound.CODEC.optionalFieldOf("music").forGetter(ModifyEffectsModifier::music)
	).apply(instance, ModifyEffectsModifier::new));

	@Override
	public boolean shouldModify(BiomeSelectionContext context) {
		return this.selector.test(context);
	}

	@Override
	public void modify(BiomeSelectionContext selectionContext, BiomeModificationContext modificationContext) {
		var effectContext = modificationContext.getEffects();
		this.fogColor.ifPresent(effectContext::setFogColor);
		this.waterColor.ifPresent(effectContext::setWaterColor);
		this.waterFogColor.ifPresent(effectContext::setWaterFogColor);
		this.skyColor.ifPresent(effectContext::setSkyColor);
		this.foliageColor.ifPresent(effectContext::setFoliageColor);
		this.grassColor.ifPresent(effectContext::setGrassColor);
		this.grassColorModifier.ifPresent(effectContext::setGrassColorModifier);
		this.particleConfig.ifPresent(effectContext::setParticleConfig);
		this.ambientSound.ifPresent(effectContext::setAmbientSound);
		this.moodSound.ifPresent(effectContext::setMoodSound);
		this.additionsSound.ifPresent(effectContext::setAdditionsSound);
		this.music.ifPresent(effectContext::setMusic);
	}

	@Override
	public Identifier getCodecId() {
		return CODEC_ID;
	}
}
