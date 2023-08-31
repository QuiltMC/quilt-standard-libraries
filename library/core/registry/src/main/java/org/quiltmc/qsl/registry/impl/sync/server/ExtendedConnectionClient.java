/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.registry.impl.sync.server;

import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import org.quiltmc.qsl.registry.mixin.ServerPlayNetworkHandlerAccessor;

public interface ExtendedConnectionClient {
	void quilt$addUnknownEntry(Registry<?> registry, Object entry);
	boolean quilt$isUnknownEntry(Registry<?> registry, Object entry);
	boolean quilt$understandsOptional();
	void quilt$setUnderstandsOptional();

	void quilt$setModProtocol(String id, int version);
	int quilt$getModProtocol(String id);

	static ExtendedConnectionClient from(ServerPlayNetworkHandler handler) {
		return (ExtendedConnectionClient) ((ServerPlayNetworkHandlerAccessor) handler).getConnection();
	}
}
