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

import java.util.LinkedHashMap;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.util.registry.DynamicRegistryManager;

import org.quiltmc.qsl.registry.impl.SignalingMemoizingSupplier;

@Mixin(DynamicRegistryManager.class)
public interface DynamicRegistryManagerMixin {
	@Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;make(Ljava/util/function/Supplier;)Ljava/lang/Object;"))
	private static Object quilt$makeInfosMutable(java.util.function.Supplier<Object> factory) {
		var obj = factory.get();
		if (obj instanceof ImmutableMap<?,?> map) {
			return new LinkedHashMap<>(map);
		} else {
			throw new AssertionError("Util.make for DynamicRegistryManager.INFOS didn't return an ImmutableMap. This should never happen!!!");
		}
	}

	@SuppressWarnings("Guava")
	@Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/google/common/base/Suppliers;memoize(Lcom/google/common/base/Supplier;)Lcom/google/common/base/Supplier;",
			remap = false))
	private static Supplier<Object> quilt$replaceBuiltinSupplier(Supplier<Object> delegate) {
		return new SignalingMemoizingSupplier<>(delegate);
	}
}
