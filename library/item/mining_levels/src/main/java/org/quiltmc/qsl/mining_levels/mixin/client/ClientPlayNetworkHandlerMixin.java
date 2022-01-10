/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.mining_levels.mixin.client;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.SynchronizeTagsS2CPacket;
import org.objectweb.asm.Opcodes;
import org.quiltmc.qsl.mining_levels.impl.MiningLevelManagerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
abstract class ClientPlayNetworkHandlerMixin {
	@Inject(method = "onSynchronizeTags", at = @At("RETURN"), slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;tagManager:Lnet/minecraft/tag/TagManager;", opcode = Opcodes.PUTFIELD)))
	private void quilt$clearMiningLevelCache(SynchronizeTagsS2CPacket packet, CallbackInfo info) {
		MiningLevelManagerImpl.clearCache();
	}
}
