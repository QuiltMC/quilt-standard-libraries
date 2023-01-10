package org.quiltmc.qsl.chat.api;

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

	enum QuiltMetaMessageType {
		MESSAGE_TYPE,
		SIDE,
		DIRECTION
	}
}
