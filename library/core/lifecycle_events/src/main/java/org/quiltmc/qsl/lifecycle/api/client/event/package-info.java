/*
 * Copyright 2021 The Quilt Project
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
 * Events to track the lifecycle of a Minecraft client.
 * <p>
 * The events in this package pertain to the logical Minecraft client, which handles input and output, rendering,
 * connections to a server and starting/stopping the client's own integrated server. A Minecraft client operates using a
 * tick loop and events are executed as the tick loop runs.
 * <p>
 * <h2>The Minecraft client singleton and lifecycle events</h2>
 * <p>
 * The {@link net.minecraft.client.MinecraftClient#getInstance() Minecraft client singleton} may be accessed at any time
 * during mod initialization, however the singleton will be in an incomplete state during mod initialization.
 * Many client facilities will not be completely setup at that point. The lifecycle events in this package are useful
 * for be notified when most client facilities have been initialized and therefore can be safely interfaced with.
 * <p>
 * The events in {@link org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents} are executed during client
 * initialization or shutdown.
 * <p>
 * The events in {@link org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents} are executed as the tick loop is
 * iterated.
 * <p>
 * To track initialization and shutdown of the client's integrated server, use
 * {@link org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents}.
 *
 * @see org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents
 * @see org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents
 * @see org.quiltmc.qsl.lifecycle.api.client.event.ClientWorldTickEvents
 */

package org.quiltmc.qsl.lifecycle.api.client.event;
