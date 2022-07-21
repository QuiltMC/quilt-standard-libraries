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

package org.quiltmc.qsl.component.test.component;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;

import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.component.SyncedComponent;
import org.quiltmc.qsl.component.api.component.TickingComponent;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.api.sync.codec.NetworkCodec;

public class SaveFloatComponent extends DefaultFloatComponent implements TickingComponent, SyncedComponent {
	private final Runnable syncOperation;

	public SaveFloatComponent(Component.Operations ops) {
		super(ops);
		this.syncOperation = ops.syncOperation();
	}

	@Override
	public void tick(ComponentProvider provider) {
		this.set(this.get() + 50);
		this.save();
		if (((ServerWorld) provider).getTime() % 100 == 0) {
			this.sync();
		}
	}

	@Override
	public void writeToBuf(PacketByteBuf buf) {
		NetworkCodec.FLOAT.encode(buf, this.get());
	}

	@Override
	public void readFromBuf(PacketByteBuf buf) {
		this.set(NetworkCodec.FLOAT.decode(buf));
	}

	@Override
	public @Nullable Runnable getSyncOperation() {
		return this.syncOperation;
	}
}
