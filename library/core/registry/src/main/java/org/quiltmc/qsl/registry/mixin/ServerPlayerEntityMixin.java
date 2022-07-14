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

package org.quiltmc.qsl.registry.mixin;

import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.quiltmc.qsl.registry.impl.sync.DelayedPacketsHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements DelayedPacketsHolder {
	@Unique
	private List<CustomPayloadC2SPacket> quilt$delayedPackets;

	@Override
	public void quilt$setPacketList(List<CustomPayloadC2SPacket> packetList) {
		this.quilt$delayedPackets = packetList;
	}

	@Override
	public List<CustomPayloadC2SPacket> quilt$getPacketList() {
		return this.quilt$delayedPackets;
	}
}
