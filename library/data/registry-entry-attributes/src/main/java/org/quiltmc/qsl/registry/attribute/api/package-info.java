/**
 * <h2>Registry Item Attributes</h2>
 *
 * A simple API for adding arbitrary values to any object instances managed by a {@link net.minecraft.util.registry.Registry Registry}.<p>
 *
 * {@link org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttribute RegistryEntryAttribute} contains a number of methods for creating
 * new attributes.<p>
 *
 * To retrieve an attribute value, first get the attribute holder using
 * {@link org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttributeHolder#get(net.minecraft.util.registry.Registry) RegistryEntryAttributeHolder.get(Registry)},
 * then use {@link org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttributeHolder#getValue(java.lang.Object, RegistryEntryAttribute)}
 * to get the value assigned to the specified registry entry.<p>
 *
 * <h3>Vocabulary</h3>
 *
 * <ul>
 *     <li>Attribute values set in-code (using
 *     {@link org.quiltmc.qsl.registry.attribute.api.RegistryExtensions#registerWithAttributes(net.minecraft.util.registry.Registry, net.minecraft.util.Identifier, java.lang.Object, java.util.function.Consumer)})
 *     are referred to as "built-in" values.</li>
 * </ul>
 *
 *
 * <h3>Data-Driven Capabilities</h3>
 *
 * All attributes declared using this API can be set using a data pack! Simply create the following file:<br>
 * {@code data/<attribute_namespace>/attributes/<registry_key_path>/<attribute_name>.json}<p>
 *
 * These "attribute maps" use a tag-like format, with a {@code replace} property that allows replacing all (non-built-in) values.<p>
 *
 * The {@code values} object functions as a map, mapping registry entry IDs to attribute values.<p>
 *
 * For example, say we have an {@code Integer} attribute for the {@code Item} registry with an ID of {@code example_mod:coolness}.<br>
 * To set Netherite Pickaxe's coolness to 20, we create this file at {@code data/example_mod/attributes/minecraft/item/coolness.json}:<pre><code>
 * {
 *   "replace": false,
 *   "values": {
 *     "minecraft:netherite_pickaxe": 20
 *   }
 * }</code></pre>
 */
package org.quiltmc.qsl.registry.attribute.api;
