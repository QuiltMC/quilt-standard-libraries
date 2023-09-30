/*
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.entity.networking.test;

import java.util.Objects;

import net.fabricmc.api.EnvType;

import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.entity.networking.api.tracked_data.QuiltTrackedDataHandlerRegistry;

public class TrackedDataTestInitializer implements ModInitializer {
	public static final TrackedDataHandler<ParticleEffect> PARTICLE_DATA_HANDLER = new TrackedDataHandler.SimpleHandler<>() {
		@Override
		public void write(PacketByteBuf buf, ParticleEffect value) {
			buf.writeFromIterable(Registries.PARTICLE_TYPE, value.getType());
			value.write(buf);
		}

		@SuppressWarnings({"rawtypes", "unchecked"})
		@Override
		public ParticleEffect read(PacketByteBuf buf) {
			ParticleType type = buf.readFromIterable(Registries.PARTICLE_TYPE);
			return Objects.requireNonNull(type).getParametersFactory().read(type, buf);
		}
	};
	public static final TrackedDataHandler<StatusEffect> TEST_HANDLER = TrackedDataHandler.createIndexed(Registries.STATUS_EFFECT);
	public static final TrackedDataHandler<StatusEffect> BAD_EXAMPLE_HANDLER = TrackedDataHandler.createIndexed(Registries.STATUS_EFFECT);

	@Override
	public void onInitialize(ModContainer mod) {
		if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT) {
			QuiltTrackedDataHandlerRegistry.register(new Identifier("quilt_test_mod", "particle"), PARTICLE_DATA_HANDLER);
			QuiltTrackedDataHandlerRegistry.register(new Identifier("quilt_test_mod", "test"), TEST_HANDLER);
		} else {
			QuiltTrackedDataHandlerRegistry.register(new Identifier("quilt_test_mod", "test"), TEST_HANDLER);
			QuiltTrackedDataHandlerRegistry.register(new Identifier("quilt_test_mod", "particle"), PARTICLE_DATA_HANDLER);
		}

		// Dont do that
		TrackedDataHandlerRegistry.register(BAD_EXAMPLE_HANDLER);
	}
}
