/*
 * Copyright 2021-2022 QuiltMC
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

package org.quiltmc.qsl.rendering.entity.impl.client;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.jetbrains.annotations.NotNull;

import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.phase.PhaseData;

public final class ArmorProviderPhaseData<T> extends PhaseData<ArmorProviderPhaseData.Inner<T>[], ArmorProviderPhaseData<T>> {
	@SuppressWarnings("unchecked")
	public ArmorProviderPhaseData(@NotNull Identifier id) {
		super(id, (Inner<T>[]) Array.newInstance(Inner.class, 0));
	}

	public void addProvider(@NotNull T provider, @NotNull ItemConvertible... applicableItems) {
		Item[] convertedItems = new Item[applicableItems.length];
		for (int i = 0; i < applicableItems.length; i++) {
			convertedItems[i] = applicableItems[i].asItem();
		}

		int oldLength = this.data.length;
		this.data = Arrays.copyOf(data, oldLength + 1);
		this.data[oldLength] = new Inner<>(provider, convertedItems);
	}

	public record Inner<T>(@NotNull T provider, @NotNull Item @NotNull [] applicableItems) {}
}
