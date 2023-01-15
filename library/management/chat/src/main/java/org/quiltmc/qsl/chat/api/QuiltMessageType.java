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

package org.quiltmc.qsl.chat.api;

/**
 * Enums that are used to identify the type, side, and direction of various messages.
 */
public enum QuiltMessageType {
	// Actual types
	CHAT(QuiltMetaMessageType.MESSAGE_TYPE),
	SYSTEM(QuiltMetaMessageType.MESSAGE_TYPE),
	PROFILE_INDEPENDENT(QuiltMetaMessageType.MESSAGE_TYPE),

	// Sidedness
	SERVER(QuiltMetaMessageType.SIDE),
	CLIENT(QuiltMetaMessageType.SIDE),

	// Directionality
	INBOUND(QuiltMetaMessageType.DIRECTION),
	OUTBOUND(QuiltMetaMessageType.DIRECTION);

	public final QuiltMetaMessageType metaType;

	QuiltMessageType(QuiltMetaMessageType metaType) {
		this.metaType = metaType;
	}

	/**
	 * The various meta-types of the {@link QuiltMessageType} enums. Used for categorizing the different types.
	 */
	public enum QuiltMetaMessageType {
		MESSAGE_TYPE,
		SIDE,
		DIRECTION
	}
}
