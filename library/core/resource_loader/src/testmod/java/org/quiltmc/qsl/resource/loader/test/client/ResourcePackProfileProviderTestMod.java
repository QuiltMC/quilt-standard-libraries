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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;

import net.minecraft.SharedConstants;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.AbstractFileResourcePack;
import net.minecraft.resource.pack.ResourcePackProfile;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.test.mixin.client.NativeImageAccessor;

public class ResourcePackProfileProviderTestMod implements ClientModInitializer {
	private static final String PACK_NAME = "Visible Test Virtual Pack";

	@Override
	public void onInitializeClient(ModContainer mod) {
		ResourceLoader.get(ResourceType.CLIENT_RESOURCES).registerResourcePackProfileProvider((profileAdder, factory) -> {
			profileAdder.accept(ResourcePackProfile.of(
					PACK_NAME, false, TestPack::new, factory,
					ResourcePackProfile.InsertionPosition.TOP,
					text -> text.shallowCopy().append(new LiteralText(" (Virtual Provider)").formatted(Formatting.DARK_GRAY))
			));
		});
	}

	static class TestPack extends AbstractFileResourcePack {
		private static final Set<String> NAMESPACES = Set.of("minecraft");
		private static final Identifier DIRT_IDENTIFIER = new Identifier("textures/block/dirt.png");
		private final Random random = new Random();

		public TestPack() {
			super(null);
		}

		@Override
		protected boolean containsFile(String name) {
			return false;
		}

		@Override
		public Collection<Identifier> findResources(ResourceType type, String namespace, String startingPath, int maxDepth,
		                                            Predicate<String> pathFilter) {
			if (type == ResourceType.CLIENT_RESOURCES && namespace.equals(DIRT_IDENTIFIER.getNamespace())) {
				if (DIRT_IDENTIFIER.getPath().startsWith(startingPath) && pathFilter.test(DIRT_IDENTIFIER.getPath())) {
					return List.of(DIRT_IDENTIFIER);
				}
			}
			return Collections.emptyList();
		}

		@Override
		public boolean contains(ResourceType type, Identifier id) {
			return type == ResourceType.CLIENT_RESOURCES && id.equals(DIRT_IDENTIFIER);
		}

		private InputStream createRandomImage() throws IOException {
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

			var out = new ByteArrayOutputStream();
			((NativeImageAccessor) (Object) image).callWrite(Channels.newChannel(out));
			image.close();
			var in = new ByteArrayInputStream(out.toByteArray());
			out.close();
			return in;
		}

		@Override
		protected InputStream openFile(String name) throws IOException {
			if (name.equals("pack.mcmeta")) {
				var pack = String.format("""
								{"pack":{"pack_format":%d,"description":"Just testing."}}
								""",
						ResourceType.CLIENT_RESOURCES.getPackVersion(SharedConstants.getGameVersion()));
				return IOUtils.toInputStream(pack, Charsets.UTF_8);
			} else if (name.equals("pack.png")
					|| name.endsWith(DIRT_IDENTIFIER.getNamespace() + "/" + DIRT_IDENTIFIER.getPath())) {
				return this.createRandomImage();
			}

			throw new FileNotFoundException("No file :p");
		}

		@Override
		public Set<String> getNamespaces(ResourceType type) {
			return NAMESPACES;
		}

		@Override
		public String getName() {
			return PACK_NAME;
		}

		@Override
		public void close() {

		}

		@Override
		public Text getDisplayName() {
			return new LiteralText(this.getName());
		}
	}
}
