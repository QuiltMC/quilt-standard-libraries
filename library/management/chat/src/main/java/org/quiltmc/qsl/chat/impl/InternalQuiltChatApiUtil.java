package org.quiltmc.qsl.chat.impl;

import org.quiltmc.qsl.chat.api.QuiltMessageType;

import java.util.EnumSet;

public class InternalQuiltChatApiUtil {
	public static EnumSet<QuiltMessageType> s2cType(QuiltMessageType type, boolean isOnClientSide) {
		if (isOnClientSide) {
			return EnumSet.of(type, QuiltMessageType.CLIENT, QuiltMessageType.INBOUND);
		} else {
			return EnumSet.of(type, QuiltMessageType.SERVER, QuiltMessageType.OUTBOUND);
		}
	}

	public static EnumSet<QuiltMessageType> c2sType(QuiltMessageType type, boolean isOnClientSide) {
		if (isOnClientSide) {
			return EnumSet.of(type, QuiltMessageType.CLIENT, QuiltMessageType.OUTBOUND);
		} else {
			return EnumSet.of(type, QuiltMessageType.SERVER, QuiltMessageType.INBOUND);
		}
	}
}
