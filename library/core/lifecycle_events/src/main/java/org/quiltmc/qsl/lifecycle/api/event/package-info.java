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
 * Events to track the lifecycle of Minecraft.
 *
 * <p>The events in this package track the lifecycle of a logical Minecraft server. A Minecraft server operates using a
 * tick loop and events are executed as the tick loop runs.
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
