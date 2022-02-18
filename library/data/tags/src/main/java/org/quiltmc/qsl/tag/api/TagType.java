/*
 * Copyright 2021-2022 QuiltMC
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
 */
public enum TagType {
	/**
	 * Represents the default tag type.
	 * <p>
	 * The client does not provide a default.
	 */
	NORMAL,
	/**
	 * Represents a tag type which is similar to the default one,
	 * but the client provides a fallback if the server doesn't have the relevant tag.
	 * <p>
	 * If two tags with the same identifier are registered, one with this type and the other one with another,
	 * they may not have the same content as the fallback content will only be added to the one with this type.
	 */
	CLIENT_FALLBACK,
	/**
	 * Represents a client-only tag type,
	 * tags are loaded from resource packs' {@code assets} directory instead of the {@code data} directory.
	 * <p>
	 * Those tags are not present on the server, thus no syncing will happen.
	 * <p>
	 * Tags registered with this type are entirely separated from other tags, thus if two tags with the same identifier,
	 * one with this type and the other with another, they may not have the same content.
	 */
	CLIENT_ONLY;

	/**
	 * {@return whether this tag type is synchronized to the client}
	 */
	public boolean hasSync() {
		return this != CLIENT_ONLY;
	}
}
