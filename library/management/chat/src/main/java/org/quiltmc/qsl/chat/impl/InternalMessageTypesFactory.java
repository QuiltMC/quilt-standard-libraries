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

package org.quiltmc.qsl.chat.impl;

import java.util.EnumSet;

import org.quiltmc.qsl.chat.api.QuiltMessageType;

/**
 * Provides some common shortcuts for message type generation
 */
public final class InternalMessageTypesFactory {
	private InternalMessageTypesFactory() {}

	public static EnumSet<QuiltMessageType> s2cType(QuiltMessageType type, boolean isClient) {
		if (isClient) {
			return EnumSet.of(type, QuiltMessageType.CLIENT, QuiltMessageType.INBOUND);
		} else {
			return EnumSet.of(type, QuiltMessageType.SERVER, QuiltMessageType.OUTBOUND);
		}
	}

	public static EnumSet<QuiltMessageType> c2sType(QuiltMessageType type, boolean isClient) {
		if (isClient) {
			return EnumSet.of(type, QuiltMessageType.CLIENT, QuiltMessageType.OUTBOUND);
		} else {
			return EnumSet.of(type, QuiltMessageType.SERVER, QuiltMessageType.INBOUND);
		}
	}
}
