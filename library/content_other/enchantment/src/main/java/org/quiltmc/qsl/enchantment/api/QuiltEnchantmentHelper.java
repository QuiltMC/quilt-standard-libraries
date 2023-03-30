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

package org.quiltmc.qsl.enchantment.api;

import org.jetbrains.annotations.Nullable;

import org.quiltmc.qsl.enchantment.impl.EnchantmentGodClass;

/**
 * Allows modded systems that enchant things randomly to apply an enchanting context.
 */
public final class QuiltEnchantmentHelper {
	public void setContext(EnchantingContext context) {
		EnchantmentGodClass.context.set(context);
	}

	public @Nullable EnchantingContext getContext() {
		return EnchantmentGodClass.context.get();
	}

	public void clearContext() {
		EnchantmentGodClass.context.remove();
	}
}
