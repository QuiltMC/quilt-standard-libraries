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

package org.quiltmc.qsl.registry.mixin;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.server.ServerMetadata;
import org.quiltmc.qsl.registry.impl.sync.modprotocol.ModProtocolContainer;
import org.quiltmc.qsl.registry.impl.sync.modprotocol.ModProtocolImpl;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(ServerMetadata.Version.class)
public class ServerMetadataVersionMixin implements ModProtocolContainer {
	@Shadow
	@Final
	@Mutable
	public static Codec<ServerMetadata.Version> CODEC;
	@Unique
	private Map<String, IntList> quilt$modProtocol;

	@Inject(method = "createCurrent", at = @At("RETURN"))
	private static void quilt$addProtocolVersions(CallbackInfoReturnable<ServerMetadata.Version> cir) {
		var map = new HashMap<String, IntList>();
		for (var protocol : ModProtocolImpl.REQUIRED) {
			map.put(protocol.id(), protocol.versions());
		}
		((ServerMetadataVersionMixin) (Object) cir.getReturnValue()).quilt$modProtocol = map;
	}

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void quilt$extendCodec(CallbackInfo ci) {
		CODEC = ModProtocolContainer.createCodec(CODEC);
	}

	@Override
	public void quilt$setModProtocol(Map<String, IntList> map) {
		this.quilt$modProtocol = map;
	}

	@Override
	public Map<String, IntList> quilt$getModProtocol() {
		return this.quilt$modProtocol;
	}
}
