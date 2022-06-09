/**
 * <h2>The Resource Loader.</h2>
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
 * {@link org.quiltmc.qsl.resource.loader.api.ResourceLoader#registerBuiltinResourcePack(net.minecraft.util.Identifier, org.quiltmc.qsl.resource.loader.api.ResourcePackActivationType)}, or
 * {@link org.quiltmc.qsl.resource.loader.api.ResourceLoader#registerBuiltinResourcePack(net.minecraft.util.Identifier, org.quiltmc.loader.api.ModContainer, ResourcePackActivationType)}
 *
 * <p>
 * <h4>Programmer Art Resource Pack</h4>
 * The Resource Loader will inject resources into the Programmer Art resource pack for each mod that provides
 * Programmer Art resources in the {@code programmer_art} top-level directory of the mod
 * whose structure is similar to a normal resource pack.
 *
 * <p>
 * <h3>Resource Reloaders</h3>
 * The Resource Loader allows mods to register resource reloaders through
 * {@link org.quiltmc.qsl.resource.loader.api.ResourceLoader#registerReloader(IdentifiableResourceReloader)},
 * which are triggered when resources are reloaded.
 * A resource reloader can depend on another and vanilla resource reloader identifiers may be found in {@link org.quiltmc.qsl.resource.loader.api.reloader.ResourceReloaderKeys}.
 */

package org.quiltmc.qsl.resource.loader.api;

import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;
