/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.chat.api;

/**
 * Provides rollback support for various classes relating to chat security. This is mostly used in the case of cancelling
 * signed messages on the client.
 */
public interface ChatSecurityRollbackSupport {
	/**
	 * Save the current state of the system. If a state is already saved without being dropped, this logs a warning.
	 */
	default void saveState() {}

	/**
	 * Rollback the state of the system to the last time that {@link ChatSecurityRollbackSupport#saveState} was called.
	 * If no state has been saved (either from never saving it or having dropped it), this throws to make the likely
	 * error state more obvious than getting disconnected later. This method additionally drops the current saved state
	 * of the system.
	 */
	default void rollbackState() {}

	/**
	 * Drop the current saved state of the system. If no state has been saved, this logs a warning.
	 */
	default void dropSavedState() {}
}
