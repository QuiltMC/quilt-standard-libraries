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
import java.util.Set;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;

import net.minecraft.registry.DynamicRegistrySync;
import net.minecraft.registry.Holder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.registry.api.dynamic.DynamicMetaRegistry;
import org.quiltmc.qsl.registry.api.dynamic.DynamicRegistryFlag;
import org.quiltmc.qsl.registry.api.event.RegistryEvents;
import org.quiltmc.qsl.tag.api.TagRegistry;
import org.quiltmc.qsl.testing.api.game.QuiltGameTest;
import org.quiltmc.qsl.testing.api.game.QuiltTestContext;
import org.quiltmc.qsl.testing.api.game.annotation.GameTest;

public class RegistryLibDynamicRegistryTest implements QuiltGameTest, ModInitializer {
	public static final String NAMESPACE = "quilt_registry_testmod";

	private static final Identifier GREETING_A_ID = id("greeting_a");
	private static final Greetings GREETING_A = new Greetings("Welcome to Quilt!", 5);
	private static final Identifier GREETING_B_ID = id("greeting_b");
	private static final Greetings GREETING_B = new Greetings("Howdy!", 2);
	private static final TagKey<Greetings> GREETING_TEST_TAG = TagKey.of(Greetings.REGISTRY_KEY, id("test_tag"));

	public static Identifier id(String path) {
		return Identifier.of(NAMESPACE, path);
	}

	@Override
	public void onInitialize(ModContainer mod) {
		DynamicMetaRegistry.registerSynced(Greetings.REGISTRY_KEY, Greetings.CODEC, DynamicRegistryFlag.OPTIONAL);
		RegistryEvents.DYNAMIC_REGISTRY_SETUP.register(context -> context.register(Greetings.REGISTRY_KEY, GREETING_B_ID, () -> GREETING_B));
	}

	@GameTest(structureName = EMPTY_STRUCTURE)
	public void greetingsGetLoaded(QuiltTestContext ctx) {
		var greetingsRegistry = ctx.getWorld().getRegistryManager().getLookupOrThrow(Greetings.REGISTRY_KEY);

		ctx.succeedIf(() -> {
			assertTrue(ctx, DynamicRegistryFlag.isOptional(Greetings.REGISTRY_KEY.getValue()), "Registry should always have the OPTIONAL flag enabled");
			assertTrue(ctx, greetingsRegistry.containsId(GREETING_A_ID), "Registry should contain modded data value from datapack");
			assertTrue(ctx, Objects.requireNonNull(greetingsRegistry.get(GREETING_A_ID)).equals(GREETING_A), "Modded value should be properly parsed from data file");
			assertTrue(ctx, GREETING_B.equals(greetingsRegistry.get(GREETING_B_ID)), "Registry should contain modded data value from event");
		});
	}

	@GameTest(structureName = EMPTY_STRUCTURE)
	public void greetingsGetSynced(QuiltTestContext ctx) {
		ctx.succeedIf(() -> assertTrue(
				ctx,
				DynamicRegistrySync
					.streamReloadableSyncedRegistries(ctx.getWorld().getServer().getLayeredRegistryManager())
					.anyMatch(e -> e.key().equals(Greetings.REGISTRY_KEY)),
				"Modded registry key should appear in the list of synced dynamic registries"
		));
	}

	@GameTest(structureName = EMPTY_STRUCTURE)
	public void greetingsTagGetLoaded(QuiltTestContext ctx) {
		Set<TagRegistry.TagValues<Greetings>> tagValuesSet =
				TagRegistry.stream(Greetings.REGISTRY_KEY).collect(Collectors.toSet());
		ctx.failIfEver(() -> assertTrue(
				ctx, tagValuesSet.isEmpty(),
				"tagValuesSet should always be populated with at least 1 object"
		));

		ctx.succeedIf(() -> assertTrue(
				ctx,
				tagValuesSet.stream().anyMatch(tagValues -> {
					Registry<Greetings> greetingsRegistry =
							ctx.getWorld().getRegistryManager().getLookupOrThrow(Greetings.REGISTRY_KEY);
					Greetings greetingsA = greetingsRegistry.get(GREETING_A_ID);

					assertTrue(
							ctx, Objects.nonNull(greetingsRegistry.get(GREETING_A_ID)),
							"Registry should contain modded data value from datapack"
					);

					Set<Greetings> heldIds = tagValues.values().stream()
							.map(Holder::getValue)
							.collect(Collectors.toSet());
					return tagValues.key().equals(GREETING_TEST_TAG) && heldIds.contains(greetingsA);
				}),
				"tagValuesSet should always contain a tag loaded from "
					+ "tags/quilt_registry_testmod/greetings/test_tag.json, and said tag should contain a value "
					+ "pointing to GREETING_A"
		));
	}

	@GameTest(structureName = EMPTY_STRUCTURE)
	public void dynamicMetaRegistryFreezes(QuiltTestContext ctx) {
		ctx.succeedIf(() -> {
			try {
				DynamicMetaRegistry.register(RegistryKey.ofRegistry(id("a")), Codec.INT);
				throw new GameTestException(
					Text.literal("DynamicMetaRegistry should not allow registration after init"),
					(int) ctx.getTick()
				);
			} catch (IllegalStateException ignored) { }
		});
	}

	private static void assertTrue(TestContext context, boolean value, String error) {
		context.assertTrue(value, Text.literal(error));
	}
}
