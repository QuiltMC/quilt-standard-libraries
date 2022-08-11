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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.phase.PhaseData;
import org.quiltmc.qsl.base.api.phase.PhaseSorting;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class ArmorProviderManager<T> {
	private final Map<Identifier, ArmorProviderPhaseData<T>> phases = new LinkedHashMap<>();
	private final List<ArmorProviderPhaseData<T>> sortedPhases = new ArrayList<>();

	private final ListMultimap<Item, T> providersMap = MultimapBuilder.hashKeys().linkedListValues().build();

	public void addProvider(@NotNull Identifier phaseIdentifier, @NotNull T provider, @NotNull ItemConvertible... items) {
		this.getOrCreatePhase(phaseIdentifier, true).addProvider(provider, items);
		this.rebuildProvidersMap();
	}

	public void addPhaseOrdering(@NotNull Identifier firstPhase, @NotNull Identifier secondPhase) {
		if (firstPhase.equals(secondPhase)) {
			throw new IllegalArgumentException("Tried to add a phase that depends on itself.");
		}

		var first = this.getOrCreatePhase(firstPhase, false);
		var second = this.getOrCreatePhase(secondPhase, false);
		PhaseData.link(first, second);
		PhaseSorting.sortPhases(this.sortedPhases);
		this.rebuildProvidersMap();
	}

	public List<T> getProviders(Item item) {
		return this.providersMap.get(item);
	}

	private void rebuildProvidersMap() {
		this.providersMap.clear();
		for (var phase : this.sortedPhases) {
			for (var provider : phase.getData()) {
				for (var item : provider.applicableItems()) {
					this.providersMap.put(item, provider.callback());
				}
			}
		}
	}

	private @NotNull ArmorProviderPhaseData<T> getOrCreatePhase(@NotNull Identifier id, boolean sortIfCreate) {
		var phase = this.phases.get(id);

		if (phase == null) {
			phase = new ArmorProviderPhaseData<>(id);
			this.phases.put(id, phase);
			this.sortedPhases.add(phase);

			if (sortIfCreate) {
				PhaseSorting.sortPhases(this.sortedPhases);
			}
		}

		return phase;
	}
}
