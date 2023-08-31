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

package org.quiltmc.qsl.resource.loader.api;

import org.jetbrains.annotations.NotNull;

import net.minecraft.resource.pack.ResourcePackProfile;

import org.quiltmc.qsl.base.api.util.InjectedInterface;

/**
 * Represents a resource pack profile with extended metadata, injected into {@link ResourcePackProfile}.
 */
@InjectedInterface(ResourcePackProfile.class)
public interface QuiltResourcePackProfile {
	/**
	 * Gets the activation type of this resource pack.
	 * <p>
	 * This may be influenced by a {@link QuiltResourcePack#getActivationType() resource pack's activation type},
	 * but this should return {@link ResourcePackActivationType#ALWAYS_ENABLED},
	 * if {@link ResourcePackProfile#isAlwaysEnabled()} returns {@code true}.
	 *
	 * @return the activation type of this resource pack
	 */
	default @NotNull ResourcePackActivationType getActivationType() {
		return ResourcePackActivationType.NORMAL;
	}
}
