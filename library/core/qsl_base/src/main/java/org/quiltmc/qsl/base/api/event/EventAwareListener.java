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

package org.quiltmc.qsl.base.api.event;

import org.quiltmc.qsl.base.api.event.client.ClientEventAwareListener;
import org.quiltmc.qsl.base.api.event.server.DedicatedServerEventAwareListener;

/**
 * Represents an event callback aware of its uniquely associated event, may be used as an entrypoint.
 * <p>
 * In {@code quilt.mod.json}, the entrypoint is defined with {@code events} key.
 * <p>
 * Any event callback interface extending this interface can be listened using this entrypoint.
 *
 * @see ClientEventAwareListener
 * @see DedicatedServerEventAwareListener
 */
public interface EventAwareListener {}
