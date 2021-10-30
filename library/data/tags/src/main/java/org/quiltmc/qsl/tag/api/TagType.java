/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.tag.api;

/**
 * Represents tag types. Tag types define how tags are loaded and synced.
 *
 * @see org.quiltmc.qsl.tag.api
 */
public enum TagType {
	/**
	 * Represents a tag type whose tags are required to start but not to connect, they will sync to the client.
	 */
	SERVER_REQUIRED(true),
	/**
	 * Represents a tag type whose tags are required to start and to connect, they will sync if present on the server.
	 * <p>
	 * If the client has tags which are of this type, and the server does not,
	 * the client will disconnect when attempting to connect to said server.
	 */
	CLIENT_SERVER_REQUIRED(true),
	/**
	 * Represents a neutral tag type, tags are not required to start nor to connect,
	 * but they will sync if present on the server.
	 * <p>
	 * The client does not provide a default.
	 */
	NEUTRAL(false),
	/**
	 * Represents a tag type whose tags are not required to start nor to connect,
	 * but they will sync if present on the server.
	 * <p>
	 * The client provides a default in-case the server doesn't have the relevant tag.
	 */
	CLIENT_SERVER_SYNC(false),
	/**
	 * Represents a client-only tag type,
	 * tags are loaded from resource packs' {@code assets} directory instead of the {@code data} directory.
	 * <p>
	 * Those tags are not present on the server, thus no syncing will happen.
	 */
	CLIENT_ONLY(false);

	private final boolean requiredToStart;

	TagType(boolean requiredToStart) {
		this.requiredToStart = requiredToStart;
	}

	public boolean isRequiredToStart() {
		return this.requiredToStart;
	}

	public boolean isRequiredToConnect() {
		return this == CLIENT_SERVER_REQUIRED;
	}

	/**
	 * {@return whether this tag type is synchronized to the client}
	 */
	public boolean hasSync() {
		return this != CLIENT_ONLY;
	}
}
