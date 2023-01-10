package org.quiltmc.qsl.chat.api;

public final class ChatEvents {
	public static final ChatApiEvent<Void> MODIFY = new ChatApiEvent<>();
	public static final ChatApiEvent<Boolean> CANCEL = new ChatApiEvent<>();
	public static final ChatApiEvent<Void> BEFORE_IO = new ChatApiEvent<>();
	public static final ChatApiEvent<Void> AFTER_IO = new ChatApiEvent<>();
}
