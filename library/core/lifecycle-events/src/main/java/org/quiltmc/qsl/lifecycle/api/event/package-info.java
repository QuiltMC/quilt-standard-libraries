/**
 * Events to track the lifecycle of Minecraft.
 *
 * <p>The events in this package track the lifecycle of a Minecraft server. A Minecraft server operates using a tick loop,
 * so these events are executed as the tick loop runs.
 *
 * <p>The events in {@link org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents} are executed during server initialization
 * or server shutdown.
 *
 * <p>The events in {@link org.quiltmc.qsl.lifecycle.api.event.ServerTickEvents} are executed as the tick loop is iterated.
 *
 * <p>The events in {@link org.quiltmc.qsl.lifecycle.api.event.ServerWorldLoadEvents} are executed as the worlds on a
 * server are loaded or unloaded.
 *
 * @see org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents
 * @see org.quiltmc.qsl.lifecycle.api.event.ServerTickEvents
 * @see org.quiltmc.qsl.lifecycle.api.event.ServerWorldLoadEvents
 */

package org.quiltmc.qsl.lifecycle.api.event;
