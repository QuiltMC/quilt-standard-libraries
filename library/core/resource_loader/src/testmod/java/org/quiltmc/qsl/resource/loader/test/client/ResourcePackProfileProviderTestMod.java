/*
 * Copyright 2021-2022 QuiltMC
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

package org.quiltmc.qsl.resource.loader.test.client;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.texture.NativeImage;

import net.minecraft.SharedConstants;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.ResourcePackProfile;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.resource.loader.api.InMemoryResourcePack;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;

public class ResourcePackProfileProviderTestMod implements ClientModInitializer {
	private static final String PACK_NAME = "Visible Test Virtual Pack";

	@Override
	public void onInitializeClient(ModContainer mod) {
		ResourceLoader.get(ResourceType.CLIENT_RESOURCES).registerResourcePackProfileProvider((profileAdder, factory) -> {
			profileAdder.accept(ResourcePackProfile.of(
					PACK_NAME, false, TestPack::new, factory,
					ResourcePackProfile.InsertionPosition.TOP,
					text -> text.copy().append(Text.literal(" (Virtual Provider)").formatted(Formatting.DARK_GRAY))
			));
		});
	}

	static class TestPack extends InMemoryResourcePack {
		private static final Identifier DIRT_IDENTIFIER = new Identifier("textures/block/dirt.png");
		private final Random random = new Random();

		public TestPack() {
			this.putText("pack.mcmeta", String.format("""
							{"pack":{"pack_format":%d,"description":"Just testing."}}
							""",
					ResourceType.CLIENT_RESOURCES.getPackVersion(SharedConstants.getGameVersion())));
			this.putImage("pack.png", this::createRandomImage);
			this.putImage(DIRT_IDENTIFIER, this::createRandomImage);
		}

		private NativeImage createRandomImage() {
			var image = new NativeImage(16, 16, true);

			boolean t = this.random.nextBoolean();
			for (int y = 0; y < 16; y++) {
				int color = 0xff << 24;
				color |= random.nextInt(256) << 16;
				color |= random.nextInt(256) << 8;
				color |= random.nextInt(256);
				for (int x = 0; x < 16; x++) {
					image.setPixelColor(t ? x : y, t ? y : x, color);
				}
			}

			return image;
		}

		@Override
		public String getName() {
			return PACK_NAME;
		}

		@Override
		public @NotNull Text getDisplayName() {
			return Text.of(this.getName());
		}
	}
}
