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

package org.quiltmc.qsl.chat.impl;

import net.minecraft.network.message.MessageChain;

import java.util.WeakHashMap;

// This is the WORST hack i have ever written in a minecraft mod
// we *need* access to the `MessageLink link` field on `MessageChain` if we want to rollback signing
// you cant access it from just lambda, but thats all we have access to
// (MessageChain references are just lost outside of these lambdas)
// so, we create a weak map from packers/unpackers to the message chain that originated them
// and do lookups through that
// - silver
public final class MessageChainReverseLookup {
	private static final WeakHashMap<MessageChain.Packer, MessageChain> packerToChain = new WeakHashMap<>();
	private static final WeakHashMap<MessageChain.Unpacker, MessageChain> unpackerToChain = new WeakHashMap<>();

	private MessageChainReverseLookup() { throw new IllegalStateException("Cannot instantiate MessageChainLookup"); }

	public static void registerPacker(MessageChain.Packer packer, MessageChain messageChain) {
		packerToChain.put(packer, messageChain);
	}

	public static void registerUnpacker(MessageChain.Unpacker unpacker, MessageChain messageChain) {
		unpackerToChain.put(unpacker, messageChain);
	}

	public static MessageChain getChainFromPacker(MessageChain.Packer packer) {
		return packerToChain.get(packer);
	}

	public static MessageChain getChainFromUnpacker(MessageChain.Unpacker unpacker) {
		return unpackerToChain.get(unpacker);
	}
}
