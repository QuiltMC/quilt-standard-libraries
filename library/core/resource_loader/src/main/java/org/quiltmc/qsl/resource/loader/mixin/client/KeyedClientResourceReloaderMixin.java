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

package org.quiltmc.qsl.resource.loader.mixin.client;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.PeriodicNotificationManager;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.resource.FoliageColormapResourceSupplier;
import net.minecraft.client.resource.GrassColormapResourceSupplier;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.client.resource.VideoWarningManager;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.search.SearchManager;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.texture.PaintingManager;
import net.minecraft.client.texture.SpriteAtlasHolder;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;
import org.quiltmc.qsl.resource.loader.api.reloader.ResourceReloaderKeys;

@ClientOnly
@Mixin({
		/* public */
		BakedModelManager.class, BlockEntityRenderDispatcher.class, BlockRenderManager.class, BuiltinModelItemRenderer.class,
		EntityModelLoader.class, EntityRenderDispatcher.class, GrassColormapResourceSupplier.class, FoliageColormapResourceSupplier.class,
		FontManager.class, LanguageManager.class, ItemRenderer.class, ParticleManager.class, PaintingManager.class,
		StatusEffectSpriteManager.class, SoundManager.class, SplashTextResourceSupplier.class, TextureManager.class,
		SpriteAtlasHolder.class,
		/* private */
		GameRenderer.class, WorldRenderer.class, VideoWarningManager.class, PeriodicNotificationManager.class, SearchManager.class
})
public abstract class KeyedClientResourceReloaderMixin implements IdentifiableResourceReloader {
	@Unique
	private Identifier quilt$id;

	@Override
	@SuppressWarnings({"ConstantConditions"})
	public @NotNull Identifier getQuiltId() {
		if (this.quilt$id == null) {
			Object self = this;

			if (self instanceof BakedModelManager) {
				this.quilt$id = ResourceReloaderKeys.Client.MODELS;
			} else if (self instanceof BlockEntityRenderDispatcher) {
				this.quilt$id = ResourceReloaderKeys.Client.BLOCK_ENTITY_RENDERERS;
			} else if (self instanceof BlockRenderManager) {
				this.quilt$id = ResourceReloaderKeys.Client.BLOCK_RENDER_MANAGER;
			} else if (self instanceof BuiltinModelItemRenderer) {
				this.quilt$id = ResourceReloaderKeys.Client.BUILTIN_ITEM_MODELS;
			} else if (self instanceof EntityModelLoader) {
				this.quilt$id = ResourceReloaderKeys.Client.ENTITY_MODELS;
			} else if (self instanceof EntityRenderDispatcher) {
				this.quilt$id = ResourceReloaderKeys.Client.ENTITY_RENDERERS;
			} else if (self instanceof StatusEffectSpriteManager) {
				this.quilt$id = ResourceReloaderKeys.Client.STATUS_EFFECTS;
			} else if (self instanceof SoundManager) {
				this.quilt$id = ResourceReloaderKeys.Client.SOUNDS;
			} else if (self instanceof SplashTextResourceSupplier) {
				this.quilt$id = ResourceReloaderKeys.Client.SPLASH_TEXTS;
			} else if (self instanceof LanguageManager) {
				this.quilt$id = ResourceReloaderKeys.Client.LANGUAGES;
			} else if (self instanceof TextureManager) {
				this.quilt$id = ResourceReloaderKeys.Client.TEXTURES;
			} else if (self instanceof ItemRenderer) {
				this.quilt$id = ResourceReloaderKeys.Client.ITEM_RENDERER;
			} else if (self instanceof GrassColormapResourceSupplier) {
				this.quilt$id = ResourceReloaderKeys.Client.GRASS_COLORMAP;
			} else if (self instanceof FoliageColormapResourceSupplier) {
				this.quilt$id = ResourceReloaderKeys.Client.FOLIAGE_COLORMAP;
			} else if (self instanceof PaintingManager) {
				this.quilt$id = ResourceReloaderKeys.Client.PAINTINGS;
			} else if (self instanceof ParticleManager) {
				this.quilt$id = ResourceReloaderKeys.Client.PARTICLES;
			} else if (self instanceof FontManager) {
				this.quilt$id = ResourceReloaderKeys.Client.FONTS;
			} else if (self instanceof SpriteAtlasHolder) {
				this.quilt$id = ResourceReloaderKeys.Client.SPRITE_ATLASES;
			} else {
				this.quilt$id = new Identifier("private/" + self.getClass().getSimpleName().toLowerCase(Locale.ROOT));
			}
		}

		return this.quilt$id;
	}
}
