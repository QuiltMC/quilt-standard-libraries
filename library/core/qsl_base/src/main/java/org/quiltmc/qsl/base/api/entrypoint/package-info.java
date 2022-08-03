/**
 * <h2>The Mod Entrypoints.</h2>
 * <p>
 * The Quilt Base API module provides three basic general-purpose entrypoints for mods:
 * <ul>
 *     <li>{@linkplain org.quiltmc.qsl.base.api.entrypoint.ModInitializer ModInitializer},
 *     a general-purpose mod initializer, ran before registries are frozen.</li>
 *     <li>{@linkplain org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer ClientModInitializer}, a client-side mod initializer.</li>
 *     <li>{@linkplain org.quiltmc.qsl.base.api.entrypoint.server.DedicatedServerModInitializer DedicatedServerModInitializer},
 *     a dedicated-server only mod initializer.</li>
 * </ul>
 * <p>
 * The Quilt Base API module also provides three event-based entrypoints for mods,
 * which some events may support by implementing any of the following interfaces:
 * <ul>
 *     <li>{@linkplain org.quiltmc.qsl.base.api.event.EventAwareListener EventAwareListener}, marks an event callback as usable as an entrypoint.</li>
 *     <li>{@linkplain org.quiltmc.qsl.base.api.event.client.ClientEventAwareListener ClientEventAwareListener},
 *     marks an event callback as usable as a client-side entrypoint.</li>
 *     <li>{@linkplain org.quiltmc.qsl.base.api.event.server.DedicatedServerEventAwareListener DedicatedServerEventAwareListener},
 *     marks an event callback as usable as a dedicated-server only entrypoint.</li>
 * </ul>
 * <p>
 * More entrypoints may be defined by other QSL modules.
 *
 * @see org.quiltmc.qsl.base.api.entrypoint.ModInitializer
 * @see org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer
 * @see org.quiltmc.qsl.base.api.entrypoint.server.DedicatedServerModInitializer
 */

package org.quiltmc.qsl.base.api.entrypoint;
