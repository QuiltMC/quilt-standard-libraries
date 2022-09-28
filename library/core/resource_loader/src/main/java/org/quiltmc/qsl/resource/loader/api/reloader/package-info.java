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
