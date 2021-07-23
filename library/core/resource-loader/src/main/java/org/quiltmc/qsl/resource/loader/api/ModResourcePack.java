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

package org.quiltmc.qsl.resource.loader.api;

import net.fabricmc.loader.api.metadata.ModMetadata;

import net.minecraft.resource.ResourcePack;

/**
 * Interface implemented by mod-provided resource packs.
 */
public interface ModResourcePack extends ResourcePack {
	/**
	 * Returns the mod metadata of the mod providing this resource pack.
	 *
	 * @return the ModMetadata object associated with the mod providing this
	 * resource pack
	 */
	ModMetadata getQuiltModMetadata();
}
