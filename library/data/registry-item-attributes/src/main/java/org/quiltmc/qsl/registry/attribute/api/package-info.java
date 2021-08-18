/**
 * <h2>Registry Item Attributes</h2>
 *
 * A simple API for adding arbitrary values to any object instances managed by a {@link net.minecraft.util.registry.Registry Registry}.<p>
 *
 * {@link org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttribute RegistryItemAttribute} contains a number of methods for creating
 * a new attribute.<p>
 *
 * To retrieve an attribute value, fist get the attribute holder using
 * {@link org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttributeHolder#get(net.minecraft.util.registry.Registry) RegistryItemAttributeHolder.get(Registry)},
 * then use {@link org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttributeHolder#getValue(java.lang.Object, RegistryEntryAttribute)}
 * to get the value assigned to the specified registry entry.<p>
 *
 * <p>
 *
 * <h3>Vocabulary</h3>
 *
 * <ul>
 *     <li>Attribute values set in-code (using
 *     {@linkplain org.quiltmc.qsl.registry.attribute.api.RegistryExtensions#registerWithAttributes(net.minecraft.util.registry.Registry, net.minecraft.util.Identifier, java.lang.Object, java.util.function.Consumer) this extension method})
 *     are referred to as "built-in" values.</li>
 * </ul>
 *
 *
 * <h3>Data Driven Capabilities</h3>
 *
 * All attributes declared using this API can be set using a data pack! Simply create the following file:<br>
 * {@code data/<attribute namespace>/attributes/<registry key path>}<p>
 *
 * These "attribute maps" use a tag-like format, with a {@code replace} property that allows replacing all (non-built-in) values.<p>
 *
 * The {@code values} object functions as a map, mapping registry entry IDs to attribute values.
 */
package org.quiltmc.qsl.registry.attribute.api;
