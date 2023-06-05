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

import java.util.WeakHashMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.message.MessageChain;

/**
 * Provides a method to get a {@link ChatSecurityRollbackSupport} from a {@link MessageChain.Packer} or {@link MessageChain.Unpacker}.
 * This class has been provided as API in the case that a user wishes to replace or extend the builtin chat signing system.
 * In relevant locations, the chat api will use this class to properly handle cancelling messages.
 * <p>
 * It is not necessary to remove any registered values from this class, the implementation uses weak references.
 */
public final class MessageChainLookup {
	private static final WeakHashMap<MessageChain.Packer, ChatSecurityRollbackSupport> packerToChain = new WeakHashMap<>();
	private static final WeakHashMap<MessageChain.Unpacker, ChatSecurityRollbackSupport> unpackerToChain = new WeakHashMap<>();

	private MessageChainLookup() { throw new IllegalStateException("Cannot instantiate MessageChainLookup"); }

	public static void registerPacker(MessageChain.Packer packer, ChatSecurityRollbackSupport chatSecurityObject) {
		packerToChain.put(packer, chatSecurityObject);
	}

	public static void registerUnpacker(MessageChain.Unpacker unpacker, ChatSecurityRollbackSupport chatSecurityObject) {
		unpackerToChain.put(unpacker, chatSecurityObject);
	}

	@Nullable
	public static ChatSecurityRollbackSupport getFromPacker(MessageChain.Packer packer) {
		return packerToChain.get(packer);
	}

	@Nullable
	public static ChatSecurityRollbackSupport getFromUnpacker(MessageChain.Unpacker unpacker) {
		return unpackerToChain.get(unpacker);
	}
}
