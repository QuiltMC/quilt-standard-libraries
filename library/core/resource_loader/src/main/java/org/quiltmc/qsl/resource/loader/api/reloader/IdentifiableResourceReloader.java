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

import org.jetbrains.annotations.NotNull;

import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;

/**
 * Interface for "identifiable" resource reloaders.
 * <p>
 * "Identifiable" resource reloaders have a unique identifier, which can be depended on,
 * and can provide dependencies that they would like to see executed before themselves.
 *
 * @see ResourceReloaderKeys
 */
public interface IdentifiableResourceReloader extends ResourceReloader {
	/**
	 * {@return the unique identifier of this resource reloader}
	 */
	@NotNull Identifier getQuiltId();
}
