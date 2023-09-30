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

/**
 * <h2>The Quilt Tags API.</h2>
 *
 * <p>
 * <h3>What are tags?</h3>
 * Tags are a way to have dynamic lists of registry entries controlled by data packs.
 * For example, Minecraft uses tags to identify all fence blocks in the game to determine if they can connect to each other.
 * <p>
 * By default, tags are available for every registry in the game, and they are entirely controlled by the server.
 * <p>
 * The goal of this API is to let you use client-only tags, client fallbacks for server-controlled tags,
 * and give you the ability to iterate through tags.
 *
 * <p>
 * <h3>Tag Types</h3>
 * {@link org.quiltmc.qsl.tag.api.TagType} allows categorizing tags and defining extra behavior.
 * Those types allow defining if a tag has a client fallback, if a tag is only loaded on the client, etc.
 *
 * <p>
 * <h3>Use the new tag types</h3>
 * To use one of the new tag types, use {@link org.quiltmc.qsl.tag.api.QuiltTagKey#of(net.minecraft.registry.RegistryKey, net.minecraft.util.Identifier, org.quiltmc.qsl.tag.api.TagType)}.
 * This lets you create a new {@link net.minecraft.registry.tag.TagKey} but with a specific {@link org.quiltmc.qsl.tag.api.TagType} instead of the default one.
 * The key can be used normally afterwards.
 *
 * <p>
 * <h3>Iterate through tags</h3>
 * The {@link org.quiltmc.qsl.tag.api.TagRegistry} contains utility methods to iterate through the different available tags.
 */

package org.quiltmc.qsl.tag.api;
