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

package org.quiltmc.qsl.entity.test.networking;

import java.util.Objects;

import net.fabricmc.api.EnvType;

import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.entity.extensions.api.networking.QuiltTrackedDataHandlerRegistry;

public class TrackedDataTestInitializer implements ModInitializer {
	public static final TrackedDataHandler<StatusEffect> TEST_HANDLER = TrackedDataHandler.create(PacketCodecs.entryOf(Registries.STATUS_EFFECT));
	public static final TrackedDataHandler<StatusEffect> TEST2_HANDLER = TrackedDataHandler.create(PacketCodecs.entryOf(Registries.STATUS_EFFECT));
	public static final TrackedDataHandler<StatusEffect> BAD_EXAMPLE_HANDLER = TrackedDataHandler.create(PacketCodecs.entryOf(Registries.STATUS_EFFECT));

	@Override
	public void onInitialize(ModContainer mod) {
		if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT) {
			QuiltTrackedDataHandlerRegistry.register(Identifier.of("quilt_test_mod", "test2"), TEST2_HANDLER);
			QuiltTrackedDataHandlerRegistry.register(Identifier.of("quilt_test_mod", "test"), TEST_HANDLER);
		} else {
			QuiltTrackedDataHandlerRegistry.register(Identifier.of("quilt_test_mod", "test"), TEST_HANDLER);
			QuiltTrackedDataHandlerRegistry.register(Identifier.of("quilt_test_mod", "test2"), TEST2_HANDLER);
		}

		// Dont do that
		TrackedDataHandlerRegistry.register(BAD_EXAMPLE_HANDLER);
	}
}
