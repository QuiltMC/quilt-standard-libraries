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

import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import org.quiltmc.qsl.data.callback.api.predicate.CodecAwarePredicate;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModificationContext;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifier;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectionContext;

/**
 * A biome modifier that modifies the weather of a biome.
 * <p>
 * The biome modifier identifier is {@code quilt:modify_weather}.
 *
 * @see BiomeModificationContext.WeatherContext
 */
public record ModifyWeatherModifier(
		CodecAwarePredicate<BiomeSelectionContext> selector,
		Optional<Float> downfall, Optional<Float> temperature,
		Optional<Boolean> hasPrecipitation,
		Optional<Biome.TemperatureModifier> temperatureModifier
) implements BiomeModifier {
	public static final Identifier CODEC_ID = new Identifier("quilt", "modify_weather");
	public static final Codec<ModifyWeatherModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BiomeModifier.BIOME_SELECTOR_CODEC.fieldOf("selector").forGetter(ModifyWeatherModifier::selector),
			Codec.FLOAT.optionalFieldOf("downfall").forGetter(ModifyWeatherModifier::downfall),
			Codec.FLOAT.optionalFieldOf("temperature").forGetter(ModifyWeatherModifier::temperature),
			Codec.BOOL.optionalFieldOf("has_precipitation").forGetter(ModifyWeatherModifier::hasPrecipitation),
			Biome.TemperatureModifier.CODEC.optionalFieldOf("temperature_modifier").forGetter(ModifyWeatherModifier::temperatureModifier)
	).apply(instance, ModifyWeatherModifier::new));

	@Override
	public boolean shouldModify(BiomeSelectionContext context) {
		return this.selector.test(context);
	}

	@Override
	public void modify(BiomeSelectionContext selectionContext, BiomeModificationContext modificationContext) {
		var weatherContext = modificationContext.getWeather();
		this.downfall.ifPresent(weatherContext::setDownfall);
		this.temperature.ifPresent(weatherContext::setTemperature);
		this.hasPrecipitation.ifPresent(weatherContext::setHasPrecipitation);
		this.temperatureModifier.ifPresent(weatherContext::setTemperatureModifier);
	}

	@Override
	public Identifier getCodecId() {
		return CODEC_ID;
	}
}
