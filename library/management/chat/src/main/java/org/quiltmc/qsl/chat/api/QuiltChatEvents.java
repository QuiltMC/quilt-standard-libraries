package org.quiltmc.qsl.chat.api;

import org.quiltmc.qsl.chat.impl.ChatApiEvent;
import org.quiltmc.qsl.chat.impl.ChatApiVoidEvent;

public final class QuiltChatEvents {
	public static final ChatApiVoidEvent MODIFY = new ChatApiVoidEvent();
	public static final ChatApiEvent<Boolean> CANCEL = new ChatApiEvent<>();
	public static final ChatApiVoidEvent BEFORE_IO = new ChatApiVoidEvent();
	public static final ChatApiVoidEvent AFTER_IO = new ChatApiVoidEvent();
}
