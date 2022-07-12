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

package org.quiltmc.qsl.component.api.components;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.impl.sync.codec.NetworkCodec;

public class SyncedGenericComponent<T, E extends NbtElement> extends GenericComponent<T> implements SyncedComponent {
	private final NetworkCodec<T> networkCodec;
	@Nullable
	private final Runnable syncOperation;

	protected SyncedGenericComponent(
			@Nullable Runnable saveOperation, @Nullable Runnable syncOperation, Codec<T> codec, NetworkCodec<T> networkCodec) {
		super(saveOperation, codec);
		this.syncOperation = syncOperation;
		this.networkCodec = networkCodec;
	}

	@Override
	public void writeToBuf(PacketByteBuf buf) {
		this.networkCodec.encode(buf, this.value);
	}

	@Override
	public void readFromBuf(PacketByteBuf buf) {
		this.networkCodec.decode(buf).ifJust(t -> this.value = t);
	}

	@Override
	public @Nullable Runnable getSyncOperation() {
		return this.syncOperation;
	}
}
