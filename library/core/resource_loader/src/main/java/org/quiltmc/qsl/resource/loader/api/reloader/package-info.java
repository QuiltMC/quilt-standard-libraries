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

/**
 * <h2>Resource Reloaders APIs.</h2>
 *
 * <p>
 * This package contains APIs related to {@link net.minecraft.resource.ResourceReloader ResourceReloaders}.
 *
 * <p>
 * <h3>Resource Reloader Extensions</h3>
 * All resource reloaders registered with {@link org.quiltmc.qsl.resource.loader.api.ResourceLoader#registerReloader(IdentifiableResourceReloader)}
 * must implement {@link org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader}, which is necessary to ensure the ability of
 * re-ordering resource reloaders.
 * <p>
 * Two simplified implementations of resource reloader are provided:
 * <ul>
 *     <li>{@link org.quiltmc.qsl.resource.loader.api.reloader.SimpleResourceReloader}</li>
 *     <li>{@link org.quiltmc.qsl.resource.loader.api.reloader.SimpleSynchronousResourceReloader}</li>
 * </ul>
 *
 * @see org.quiltmc.qsl.resource.loader.api.reloader.ResourceReloaderKeys Keys of the Vanilla resource reloaders which can be used to reorder resource reloaders.
 */

package org.quiltmc.qsl.resource.loader.api.reloader;
