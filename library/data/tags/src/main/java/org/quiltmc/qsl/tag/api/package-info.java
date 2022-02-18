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
 * The goal of this API is to give to modders the ability to iterate through tags, have client-only tags,
 * and client fallbacks for server-controlled tags.
 *
 * <p>
 * <h3>Tag Types</h3>
 * {@link org.quiltmc.qsl.tag.api.TagType} allows categorizing tags and defining extra behavior.
 * Those types allow defining if a tag has a client fallback, if a tag is only loaded on the client, etc.
 *
 * <p>
 * <h3>Use the new tag types</h3>
 * To use one of the new tag types, use {@link org.quiltmc.qsl.tag.api.QuiltTagKey#create(net.minecraft.util.registry.RegistryKey, net.minecraft.util.Identifier, org.quiltmc.qsl.tag.api.TagType)}.
 * It allows to create a new {@link net.minecraft.tag.TagKey} but with a specific {@link org.quiltmc.qsl.tag.api.TagType} instead of the default one.
 * The key can be used normally afterwards.
 *
 * <p>
 * <h3>Iterate through tags</h3>
 * The {@link org.quiltmc.qsl.tag.api.TagRegistry} contains utility methods to iterate through the different available tags.
 */

package org.quiltmc.qsl.tag.api;
