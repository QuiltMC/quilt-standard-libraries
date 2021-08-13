/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.resource.loader.mixin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.resource.loader.impl.ModNioResourcePack;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;

/**
 * This mixin has the goal to fix resource leaking of mod resources into the default resource pack.
 * <p>
 * This well-known bug caused many issues of Vanilla tags being overwritten by mods' tags.
 */
@Mixin(DefaultResourcePack.class)
public abstract class DefaultResourcePackMixin {
	// Redirects all resource access to the MC resource pack.
	@Unique
	final ModNioResourcePack internalPack = this.locateAndLoad();

	@SuppressWarnings({"ConstantConditions", "EqualsBetweenInconvertibleTypes"})
	@Unique
	private ModNioResourcePack locateAndLoad() {
		return ResourceLoaderImpl.locateAndLoadDefaultResourcePack(
				this.getClass().equals(DefaultResourcePack.class) ?
						ResourceType.SERVER_DATA : ResourceType.CLIENT_RESOURCES
		);
	}

	/**
	 * @author QuiltMC, LambdAurora
	 * @reason Rewrite default resource access to avoid resource leaking.
	 */
	@Overwrite
	public boolean contains(ResourceType type, Identifier id) {
		return this.internalPack.contains(type, id);
	}

	/**
	 * @author QuiltMC, LambdAurora
	 * @reason Rewrite default resource access to avoid resource leaking.
	 */
	@Overwrite
	public @Nullable InputStream findInputStream(ResourceType type, Identifier id) {
		try {
			return this.internalPack.open(type, id);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * @author QuiltMC, LambdAurora
	 * @reason Rewrite default resource access to avoid resource leaking.
	 */
	@Overwrite
	public @Nullable InputStream getInputStream(String path) {
		try {
			return this.internalPack.openRoot(path);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * @author QuiltMC, LambdAurora
	 * @reason Rewrite default resource access to avoid resource leaking.
	 */
	@Overwrite
	public Collection<Identifier> findResources(ResourceType type, String namespace, String prefix, int maxDepth,
												Predicate<String> pathFilter) {
		return this.internalPack.findResources(type, namespace, prefix, maxDepth, pathFilter);
	}

	@Inject(method = "close", at = @At("HEAD"), remap = false)
	private void onClose(CallbackInfo ci) {
		this.internalPack.close();
	}
}
