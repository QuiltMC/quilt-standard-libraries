/*
 * Copyright 2022 The Quilt Project
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
 * <h2>The Networking (client-side) API.</h2>
 * <p>
 * For login stage networking see {@link org.quiltmc.qsl.networking.api.client.ClientLoginNetworking}.
 * For play stage networking see {@link org.quiltmc.qsl.networking.api.client.ClientPlayNetworking}.
 * <p>
 * For events related to connection to a server see {@link org.quiltmc.qsl.networking.api.client.ClientLoginConnectionEvents} for login stage
 * or {@link org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents} for play stage.
 * <p>
 * For events related to the ability of a server to receive packets on a channel of a specific name see {@link org.quiltmc.qsl.networking.api.client.C2SPlayChannelEvents}.
 */

package org.quiltmc.qsl.networking.api.client;
