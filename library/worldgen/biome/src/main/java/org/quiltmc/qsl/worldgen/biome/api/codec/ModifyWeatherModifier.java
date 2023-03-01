/*
 * Copyright 2023 QuiltMC
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

import org.quiltmc.qsl.data.callbacks.predicate.CodecAwarePredicate;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModificationContext;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifier;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectionContext;

/**
 * A biome modifier that modifies the weather of a biome.
 * @see BiomeModificationContext.WeatherContext
 */
public record ModifyWeatherModifier(CodecAwarePredicate<BiomeSelectionContext> selector,
									Optional<Float> downfall, Optional<Float> temperature,
									Optional<Biome.Precipitation> precipitation,
									Optional<Biome.TemperatureModifier> temperatureModifier) implements BiomeModifier {

	public static final Identifier IDENTIFIER = new Identifier("quilt", "modify_weather");
	public static final Codec<ModifyWeatherModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BiomeModifier.BIOME_SELECTOR_CODEC.fieldOf("selector").forGetter(ModifyWeatherModifier::selector),
			Codec.FLOAT.optionalFieldOf("downfall").forGetter(ModifyWeatherModifier::downfall),
			Codec.FLOAT.optionalFieldOf("temperature").forGetter(ModifyWeatherModifier::temperature),
			Biome.Precipitation.CODEC.optionalFieldOf("precipitation").forGetter(ModifyWeatherModifier::precipitation),
			Biome.TemperatureModifier.CODEC.optionalFieldOf("temperature_modifier").forGetter(ModifyWeatherModifier::temperatureModifier)
	).apply(instance, ModifyWeatherModifier::new));

	@Override
	public boolean shouldModify(BiomeSelectionContext context) {
		return selector.test(context);
	}

	@Override
	public void modify(BiomeSelectionContext selectionContext, BiomeModificationContext modificationContext) {
		var weatherContext = modificationContext.getWeather();
		downfall.ifPresent(weatherContext::setDownfall);
		temperature.ifPresent(weatherContext::setTemperature);
		precipitation.ifPresent(weatherContext::setPrecipitation);
		temperatureModifier.ifPresent(weatherContext::setTemperatureModifier);
	}

	@Override
	public Identifier getCodecIdentifier() {
		return IDENTIFIER;
	}
}
