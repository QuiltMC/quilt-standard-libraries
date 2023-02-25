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

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.unmapped.C_uhbbwvga;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(C_uhbbwvga.class)
public interface DynamicRegistrySyncAccessor {
	@Accessor("f_hmxjhwjb")
	static Map<RegistryKey<? extends Registry<?>>, ?> quilt$getSyncedCodecs() {
		throw new IllegalStateException("Mixin injection failed.");
	}

	@Accessor("f_hmxjhwjb")
	static void quilt$setSyncedCodecs(Map<RegistryKey<? extends Registry<?>>, ?> syncedCodecs) {
		throw new IllegalStateException("Mixin injection failed.");
	}

	@Invoker("m_dfuklmpj")
	static <E> void quilt$invokeAddSyncedRegistry(ImmutableMap.Builder<RegistryKey<? extends Registry<?>>, ?> builder, RegistryKey<? extends Registry<E>> registryKey, Codec<E> codec) {
		throw new IllegalStateException("Mixin injection failed.");
	}
}
