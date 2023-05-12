/*
 * Copyright 2023 QuiltMC
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

package org.quiltmc.qsl.chat.impl.mixin;

import net.minecraft.network.message.LastSeenMessageTracker;

/**
 * Implements rollback support for {@link LastSeenMessageTracker#update()} so that we can "unsign" outbound messages
 * <p>
 * Signatures don't become invalid, but they are removed from the message tracker so that future ones can
 * be properly signed. Mostly used for message cancellation.
 */
public interface LastSeenMessageTrackerRollbackSupport {
	void saveState();
	void rollbackState();
	void dropSavedState();
}
