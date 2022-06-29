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

package org.quiltmc.qsl.component.impl.sync;

import com.mojang.serialization.Lifecycle;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.impl.CommonInitializer;
import org.quiltmc.qsl.component.impl.sync.header.SyncPacketHeader;

public class ComponentHeaderRegistry {
	public static final Registry<SyncPacketHeader<?>> HEADERS =
			new SimpleRegistry<>(RegistryKey.ofRegistry(CommonInitializer.id("sync_headers")), Lifecycle.experimental(), null);

	public static <P extends ComponentProvider> SyncPacketHeader<P> register(Identifier id, SyncPacketHeader<P> header) {
		return Registry.register(HEADERS, id, header);
	}
}
