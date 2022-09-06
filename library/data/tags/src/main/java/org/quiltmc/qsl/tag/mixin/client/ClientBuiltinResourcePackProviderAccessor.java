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

package org.quiltmc.qsl.tag.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.resource.ClientBuiltinResourcePackProvider;
import net.minecraft.resource.pack.DefaultResourcePack;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.resource.pack.metadata.PackResourceMetadata;

@Environment(EnvType.CLIENT)
@Mixin(ClientBuiltinResourcePackProvider.class)
public interface ClientBuiltinResourcePackProviderAccessor {
	@Accessor("DEFAULT_PACK_METADATA")
	static PackResourceMetadata getDefaultPackMetadata() {
		throw new IllegalStateException("Accessor injection failed.");
	}

	@Accessor
	DefaultResourcePack getPack();
}
