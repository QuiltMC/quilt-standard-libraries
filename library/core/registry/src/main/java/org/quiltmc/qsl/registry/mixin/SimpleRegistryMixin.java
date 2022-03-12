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

import com.mojang.serialization.Lifecycle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.util.Holder;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

import org.quiltmc.qsl.registry.impl.event.MutableRegistryEntryContextImpl;
import org.quiltmc.qsl.registry.impl.event.RegistryEventStorage;

/**
 * Stores and invokes registry events.
 */
@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<V> extends Registry<V> {
	@Unique
	private final MutableRegistryEntryContextImpl<V> quilt$entryContext = new MutableRegistryEntryContextImpl<>(this);

	protected SimpleRegistryMixin(RegistryKey<? extends Registry<V>> key, Lifecycle lifecycle) {
		super(key, lifecycle);
	}

	/**
	 * Invokes the entry add event.
	 */
	@Inject(
			method = "registerMapping",
			at = @At("RETURN")
	)
	private void quilt$invokeEntryAddEvent(int rawId, RegistryKey<V> key, V entry, Lifecycle lifecycle, boolean checkDuplicateKeys,
	                                       CallbackInfoReturnable<Holder<V>> cir) {
		this.quilt$entryContext.set(key.getValue(), entry, rawId);
		RegistryEventStorage.as(this).quilt$getEntryAddedEvent().invoker().onAdded(this.quilt$entryContext);
	}
}
