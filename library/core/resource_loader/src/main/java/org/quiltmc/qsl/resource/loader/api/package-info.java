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
 * <h2>The Resource Loader and its APIs.</h2>
 *
 * <p>
 * <h3>Quick note about vocabulary in Resource Loader and Minecraft:</h3>
 * <ul>
 *  <li>Resource Pack refers to both client-sided resource pack and data pack.</li>
 *  <li>Virtual Resource Pack refers to a resource pack that may be generated at runtime, or simply doesn't exist directly on disk.</li>
 *  <li>Group Resource Pack refers to a virtual resource pack that groups multiple resource packs together.</li>
 * </ul>
 *
 * <p>
 * <h3>Modded Resource Pack Handling</h3>
 * The Resource Loader will create a resource pack for each mod that provides resources in {@code assets} or {@code data}
 * top-level directories of the mod.
 * Those mod resource packs are grouped into the default resource pack.
 * They can override the default resource pack, but it is not recommended as it forcefully introduce changes that users
 * may not want, instead using a built-in resource pack would be better.
 *
 * <p>
 * <h4>Built-in Mod Resource Pack</h4>
 * The Resource Loader adds manually registered mod resource packs. Those resource packs are registered with
 * {@link org.quiltmc.qsl.resource.loader.api.ResourceLoader#registerBuiltinResourcePack(Identifier, ResourcePackActivationType)}, or
 * {@link org.quiltmc.qsl.resource.loader.api.ResourceLoader#registerBuiltinResourcePack(Identifier, org.quiltmc.loader.api.ModContainer, ResourcePackActivationType)}
 *
 * <p>
 * <h4>Resource Pack Injection</h4>
 * <p>
 * <h5>Resource Pack Profile Provider</h5>
 * The Resource Loader gives a method to register {@link net.minecraft.resource.pack.ResourcePackProvider ResourcePackProviders},
 * which may be used to add new resource packs that are visible to the player in the resource pack selection screens or the {@code datapack} command.
 *
 * <p>
 * <h5>Virtual Resource Packs</h5>
 * Some mods may need to rely on virtual resource packs to generate resources on the fly.
 * The Resource Loader provides utilities to work with such kind of resource packs:
 * <ul>
 *     <li>
 *         {@link org.quiltmc.qsl.resource.loader.api.ResourceLoader#getRegisterDefaultResourcePackEvent()}
 *         - an event to register resource packs that are injected into the default resource pack.
 *     </li>
 *     <li>
 *         {@link org.quiltmc.qsl.resource.loader.api.ResourceLoader#getRegisterTopResourcePackEvent()}
 *         - an event to register resource packs that are on the top of the resource pack hierarchy, those resource packs are invisible to the player.
 *     </li>
 *     <li>{@link org.quiltmc.qsl.resource.loader.api.InMemoryResourcePack} - a resource pack implementation whose resources are stored in the live memory.</li>
 *     <li>
 *         {@link org.quiltmc.qsl.resource.loader.api.GroupResourcePack} - a resource pack implementation which can be used to group multiple resource packs into one.
 *     </li>
 * </ul>
 *
 * <p>
 * <h4>Extending Vanilla Built-In Resource Packs</h4>
 * The Resource Loader will inject resources into vanilla built-in resource packs for each mod that provides
 * the resources in a top-level directory inside the mod with the pack's raw name (for example,
 * {@code programmer_art}), whose structure is similar to a normal resource pack.
 * <p>
 * The currently supported targets are:
 * <ul>
 *     <li>
 *         Programmer Art ({@code programmer_art})
 *     </li>
 *     <li>
 *         High Contrast ({@value net.minecraft.client.resource.ClientBuiltinResourcePackProvider#HIGH_CONTRAST_NAME})
 *     </li>
 * </ul>
 * <p>
 * Do note that this won't allow for overriding resources that the built-in resource packs already have.
 * <p>
 * In the case of data-packs, Vanilla has a {@code data/minecraft/datapacks} directory in which folders for each
 * built-in data-pack is present, using the same paths in mods will allow to extend those data-packs as well.
 *
 * <p>
 * <h3>Resource Reloaders</h3>
 * The Resource Loader allows mods to register resource reloaders through
 * {@link org.quiltmc.qsl.resource.loader.api.ResourceLoader#registerReloader(IdentifiableResourceReloader)},
 * which are triggered when resources are reloaded.
 * Resource reloaders can define a specific ordering through
 * {@link org.quiltmc.qsl.resource.loader.api.ResourceLoader#addReloaderOrdering(Identifier, Identifier)},
 * vanilla resource reloader identifiers may be found in {@link org.quiltmc.qsl.resource.loader.api.reloader.ResourceReloaderKeys}.
 */

package org.quiltmc.qsl.resource.loader.api;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;
