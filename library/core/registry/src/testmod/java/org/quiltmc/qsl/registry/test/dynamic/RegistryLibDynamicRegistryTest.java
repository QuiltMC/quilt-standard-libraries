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

package org.quiltmc.qsl.registry.test.dynamic;

import java.util.Objects;

import com.mojang.serialization.Codec;

import net.minecraft.registry.DynamicRegistrySync;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.registry.api.dynamic.DynamicMetaRegistry;
import org.quiltmc.qsl.registry.api.dynamic.DynamicRegistryFlag;
import org.quiltmc.qsl.registry.api.event.RegistryEvents;
import org.quiltmc.qsl.testing.api.game.QuiltGameTest;
import org.quiltmc.qsl.testing.api.game.QuiltTestContext;

public class RegistryLibDynamicRegistryTest implements QuiltGameTest, ModInitializer {
	private static final Identifier GREETING_A_ID = new Identifier("quilt_registry_testmod", "greeting_a");
	private static final Greetings GREETING_A = new Greetings("Welcome to Quilt!", 5);
	private static final Identifier GREETING_B_ID = new Identifier("quilt_registry_testmod", "greeting_b");
	private static final Greetings GREETING_B = new Greetings("Howdy!", 2);

	@Override
	public void onInitialize(ModContainer mod) {
		DynamicMetaRegistry.registerSynced(Greetings.REGISTRY_KEY, Greetings.CODEC, DynamicRegistryFlag.OPTIONAL);
		RegistryEvents.DYNAMIC_REGISTRY_SETUP.register(context -> context.register(Greetings.REGISTRY_KEY, GREETING_B_ID, () -> GREETING_B));
	}

	@GameTest(structureName = EMPTY_STRUCTURE)
	public void greetingsGetLoaded(QuiltTestContext ctx) {
		Registry<Greetings> greetingsRegistry = ctx.getWorld().getRegistryManager().get(Greetings.REGISTRY_KEY);

		ctx.succeedIf(() -> {
			ctx.assertTrue(DynamicRegistryFlag.isOptional(Greetings.REGISTRY_KEY.getValue()), "Registry should always have the OPTIONAL flag enabled");
			ctx.assertTrue(greetingsRegistry.containsId(GREETING_A_ID), "Registry should contain modded data value from datapack");
			ctx.assertTrue(Objects.requireNonNull(greetingsRegistry.get(GREETING_A_ID)).equals(GREETING_A), "Modded value should be properly parsed from data file");
			ctx.assertTrue(GREETING_B.equals(greetingsRegistry.get(GREETING_B_ID)), "Registry should contain modded data value from event");
		});
	}

	@GameTest(structureName = EMPTY_STRUCTURE)
	public void greetingsGetSynced(QuiltTestContext ctx) {
		ctx.succeedIf(() -> ctx.assertTrue(
				DynamicRegistrySync.streamReloadableSyncedRegistries(ctx.getWorld().getServer().getLayeredRegistryManager()).anyMatch(e -> e.key().equals(Greetings.REGISTRY_KEY)),
				"Modded registry key should appear in the list of synced dynamic registries"
		));
	}

	@GameTest(structureName = EMPTY_STRUCTURE)
	public void dynamicMetaregistryFreezes(QuiltTestContext ctx) {
		ctx.succeedIf(() -> {
			try {
				DynamicMetaRegistry.register(RegistryKey.ofRegistry(new Identifier("quilt_registry_testmod", "a")), Codec.INT);
				throw new GameTestException("DynamicMetaregistry should not allow registration after init");
			} catch (IllegalStateException ignored) {}
		});
	}
}
