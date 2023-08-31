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
 * Enums that are used to identify the type, side, and direction of various messages.
 */
public enum QuiltMessageType {
	// Actual types

	/**
	 * Identifies a chat message. Usually from a player.
	 */
	CHAT(QuiltMetaMessageType.MESSAGE_TYPE),

	/**
	 * Identifies a system message. Used for command results, actionbar messages, and more.
	 */
	SYSTEM(QuiltMetaMessageType.MESSAGE_TYPE),

	/**
	 * Identifies a profile independent message. Usually from /msg and similar commands.
	 */
	PROFILE_INDEPENDENT(QuiltMetaMessageType.MESSAGE_TYPE),

	// Sidedness

	/**
	 * Identifies a message that is on the server currently.
	 */
	SERVER(QuiltMetaMessageType.SIDE),

	/**
	 * Identifies a message that is on the client currently.
	 */
	CLIENT(QuiltMetaMessageType.SIDE),

	// Directionality

	/**
	 * Identifies a message that is arriving on the side it is on currently.
	 */
	INBOUND(QuiltMetaMessageType.DIRECTION),

	/**
	 * Identifies a message that is leaving the side it is on currently.
	 */
	OUTBOUND(QuiltMetaMessageType.DIRECTION);

	public final QuiltMetaMessageType metaType;

	QuiltMessageType(QuiltMetaMessageType metaType) {
		this.metaType = metaType;
	}

	/**
	 * The various meta-types of the {@link QuiltMessageType} enums. Used for categorizing the different types.
	 */
	public enum QuiltMetaMessageType {
		/**
		 * Identifies the message type of a message.
		 */
		MESSAGE_TYPE,

		/**
		 * Identifies the side a message is currently on.
		 */
		SIDE,

		/**
		 * Identifies if the message is inbound or outbound from the current side.
		 */
		DIRECTION
	}
}
