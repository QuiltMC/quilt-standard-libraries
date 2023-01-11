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

import org.quiltmc.qsl.chat.impl.ChatApiEvent;
import org.quiltmc.qsl.chat.impl.ChatApiVoidEvent;

public final class QuiltChatEvents {
	public static final ChatApiVoidEvent MODIFY = new ChatApiVoidEvent();
	public static final ChatApiEvent<Boolean> CANCEL = new ChatApiEvent<>();
	public static final ChatApiVoidEvent BEFORE_IO = new ChatApiVoidEvent();
	public static final ChatApiVoidEvent AFTER_IO = new ChatApiVoidEvent();
}
