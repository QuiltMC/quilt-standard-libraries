/**
 * <h2>The Quilt Tags API.</h2>
 *
 * <p>
 * <h3>What are tags?</h3>
 * Tags are a way to have un-hardcoded list of registry entries since the content of tags are controlled by data packs..
 * For example, Minecraft use those to identify all stairs blocks in the game, which is used for the connection mechanic.
 * <p>
 * By default tags are available for most of the static registries and are one of the earliest resource to be loaded on
 * server start.
 * <p>
 * The goal of the API is to give to modders the ability to get (and register) tags of any registries, dynamic ones included.
 *
 * <p>
 * <h3>Tag Types</h3>
 * {@link org.quiltmc.qsl.tag.api.TagType} allows to categorize tags and define extra-behavior.
 * Those types allows to define if a tag is required for a server to start, for a client to connect,
 * if a tag is only loaded on the client, etc.
 *
 * <p>
 * <h3>Get and register a tag</h3>
 * To get and/or register a tag, use {@link org.quiltmc.qsl.tag.api.TagRegistry} create methods.
 * All registered tags through this interface will auto-update their content through data/resource pack reloads.
 * You can find built-in tag registries for some of the most relevant registries, you can also create a new tag registry
 * using {@link org.quiltmc.qsl.tag.api.TagRegistry#of(net.minecraft.util.registry.RegistryKey, java.lang.String)}
 * or {@link org.quiltmc.qsl.tag.api.TagRegistry#of(java.util.function.Supplier)} if needed.
 */

package org.quiltmc.qsl.tag.api;
