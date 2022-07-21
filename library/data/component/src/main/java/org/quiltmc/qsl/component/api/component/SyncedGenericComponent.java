/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.component.api.component;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.PacketByteBuf;

import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.sync.codec.NetworkCodec;

public class SyncedGenericComponent<T> extends GenericComponent<T> implements SyncedComponent {
	private final NetworkCodec<T> networkCodec;
	@Nullable
	private final Runnable syncOperation;

	protected SyncedGenericComponent(Component.Operations ops, Codec<T> codec, NetworkCodec<T> networkCodec) {
		super(ops, codec);
		this.syncOperation = ops.syncOperation();
		this.networkCodec = networkCodec;
	}

	@Override
	public void writeToBuf(PacketByteBuf buf) {
		this.networkCodec.encode(buf, this.getValue());
	}

	@Override
	public void readFromBuf(PacketByteBuf buf) {
		this.setValue(this.networkCodec.decode(buf));
	}

	@Override
	public @Nullable Runnable getSyncOperation() {
		return this.syncOperation;
	}
}
