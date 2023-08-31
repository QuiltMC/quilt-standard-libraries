/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.resource.loader.api.reloader;

import net.minecraft.util.Identifier;

/**
 * This class contains default keys for various Minecraft resource reloaders.
 *
 * @see IdentifiableResourceReloader
 */
public final class ResourceReloaderKeys {
	/**
	 * Represents the application phase before Vanilla resource reloaders are invoked.
	 * <p>
	 * No resource reloaders are assigned to this identifier.
	 *
	 * @see org.quiltmc.qsl.resource.loader.api.ResourceLoader#addReloaderOrdering(Identifier, Identifier)
	 */
	public static final Identifier BEFORE_VANILLA = new Identifier("quilt", "before_vanilla");
	/**
	 * Represents the application phase after Vanilla resource reloaders are invoked.
	 * <p>
	 * No resource reloaders are assigned to this identifier.
	 *
	 * @see org.quiltmc.qsl.resource.loader.api.ResourceLoader#addReloaderOrdering(Identifier, Identifier)
	 */
	public static final Identifier AFTER_VANILLA = new Identifier("quilt", "after_vanilla");

	/**
	 * Keys for various client resource reloaders.
	 */
	public static final class Client {
		public static final Identifier BLOCK_ENTITY_RENDERERS = id("block_entity_renderers");
		public static final Identifier BLOCK_RENDER_MANAGER = id("block_render_manager");
		public static final Identifier BUILTIN_ITEM_MODELS = id("builtin_item_models");
		public static final Identifier ENTITY_MODELS = id("entity_models");
		public static final Identifier ENTITY_RENDERERS = id("entity_renderers");
		public static final Identifier FOLIAGE_COLORMAP = id("foliage_colormap");
		public static final Identifier FONTS = id("fonts");
		public static final Identifier GRASS_COLORMAP = id("grass_colormap");
		public static final Identifier ITEM_RENDERER = id("item_renderer");
		public static final Identifier LANGUAGES = id("languages");
		public static final Identifier MODELS = id("models");
		public static final Identifier PAINTINGS = id("paintings");
		public static final Identifier PARTICLES = id("particles");
		public static final Identifier SHADERS = id("shaders");
		public static final Identifier SOUNDS = id("sounds");
		public static final Identifier SPLASH_TEXTS = id("splash_texts");
		public static final Identifier SPRITE_ATLASES = id("sprite_atlases");
		public static final Identifier STATUS_EFFECTS = id("status_effects");
		public static final Identifier TEXTURES = id("textures");

		private Client() {
		}
	}

	/**
	 * Keys for various server resource reloaders.
	 */
	public static final class Server {
		public static final Identifier ADVANCEMENTS = id("advancements");
		public static final Identifier FUNCTIONS = id("functions");
		public static final Identifier LOOT_TABLES = id("loot_tables");
		public static final Identifier RECIPES = id("recipes");
		public static final Identifier TAGS = id("tags");

		private Server() {
		}
	}

	private ResourceReloaderKeys() {
	}

	private static Identifier id(String path) {
		return new Identifier(Identifier.DEFAULT_NAMESPACE, path);
	}
}
