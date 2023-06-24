/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.registry.attachment.test;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import org.quiltmc.qsl.registry.attachment.api.DispatchedType;

public abstract class FuncValue implements DispatchedType {
	// in a real-world application, you'd probably use a Registry for this
	public static final Map<Identifier, Codec<? extends FuncValue>> CODECS = Util.make(() ->
			ImmutableMap.<Identifier, Codec<? extends FuncValue>>builder()
					.put(SendMessageFuncValue.TYPE, SendMessageFuncValue.CODEC)
					.put(GiveStackFuncValue.TYPE, GiveStackFuncValue.CODEC)
					.build());

	protected final Identifier type;

	protected FuncValue(Identifier type) {
		this.type = type;
	}

	@Override
	public final Identifier getType() {
		return this.type;
	}

	public abstract void invoke(ServerPlayerEntity player);

	@Override
	public abstract String toString();
}
