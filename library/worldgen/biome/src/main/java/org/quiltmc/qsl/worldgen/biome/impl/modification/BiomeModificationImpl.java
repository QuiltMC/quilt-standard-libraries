/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.worldgen.biome.impl.modification;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.base.Stopwatch;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.TestOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.WorldSaveProperties;
import net.minecraft.world.biome.Biome;

import org.quiltmc.qsl.worldgen.biome.api.BiomeModificationContext;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifier;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectionContext;
import org.quiltmc.qsl.worldgen.biome.api.ModificationPhase;

@ApiStatus.Internal
public class BiomeModificationImpl {
	private static final Logger LOGGER = LoggerFactory.getLogger(BiomeModificationImpl.class);

	private static final Comparator<ModifierRecord> MODIFIER_ORDER_COMPARATOR = Comparator.<ModifierRecord>comparingInt(r -> r.phase.ordinal()).thenComparingInt(r -> r.order).thenComparing(r -> r.id);

	public static final BiomeModificationImpl INSTANCE = new BiomeModificationImpl();

	private final List<ModifierRecord> modifiers = new ArrayList<>();

	private boolean modifiersUnsorted = true;

	final BiomeModificationReloader reloader = new BiomeModificationReloader();

	private BiomeModificationImpl() {}

	public void addModifier(Identifier id, ModificationPhase phase, Predicate<BiomeSelectionContext> selector, BiConsumer<BiomeSelectionContext, BiomeModificationContext> modifier) {
		Objects.requireNonNull(selector);
		Objects.requireNonNull(modifier);

		this.modifiers.add(new ModifierRecord(phase, id, selector, modifier));
		this.modifiersUnsorted = true;
	}

	public void addModifier(Identifier id, ModificationPhase phase, Predicate<BiomeSelectionContext> selector, Consumer<BiomeModificationContext> modifier) {
		Objects.requireNonNull(selector);
		Objects.requireNonNull(modifier);

		this.modifiers.add(new ModifierRecord(phase, id, selector, modifier));
		this.modifiersUnsorted = true;
	}

	public void addModifier(Identifier id, ModificationPhase phase, BiomeModifier modifier) {
		Objects.requireNonNull(modifier);

		this.reloader.addModifier(phase, id, modifier);
		var modifierRecord = new ModifierRecord(phase, id, () -> this.reloader.getCombinedMap(phase).get(id));
		this.modifiers.add(modifierRecord);
		this.identifiedModifiers.computeIfAbsent(phase, p -> new HashMap<>()).put(id, modifierRecord);
		this.modifiersUnsorted = true;
	}

	private void addLazyModifier(Identifier id, ModificationPhase phase) {
		var modifierRecord = new ModifierRecord(phase, id, () -> this.reloader.getCombinedMap(phase).get(id));
		this.modifiers.add(modifierRecord);
		this.identifiedModifiers.computeIfAbsent(phase, p -> new HashMap<>()).put(id, modifierRecord);
		this.modifiersUnsorted = true;
	}

	public void updateIdentifiedModifiers() {
		for (var phase : this.identifiedModifiers.entrySet()) {
			List<Identifier> drop = new ArrayList<>();
			var map = this.reloader.getCombinedMap(phase.getKey());

			for (var entry : phase.getValue().entrySet()) {
				if (!map.containsKey(entry.getKey())) {
					entry.getValue().canBeDropped = true;
					drop.add(entry.getKey());
				}
			}

			drop.forEach(phase.getValue()::remove);
		}
	}

	public void addMissingModifiers() {
		for (ModificationPhase phase : ModificationPhase.values()) {
			var map = this.reloader.getCombinedMap(phase);
			var phaseMap = this.identifiedModifiers.computeIfAbsent(phase, p -> new HashMap<>());

			for (Map.Entry<Identifier, BiomeModifier> entry : map.entrySet()) {
				if (!phaseMap.containsKey(entry.getKey())) {
					this.addLazyModifier(entry.getKey(), phase);
					this.modifiersUnsorted = true;
				}
			}
		}
	}

	/**
	 * This is currently not publicly exposed but likely useful for modpack support mods.
	 */
	void changeOrder(Identifier id, int order) {
		this.modifiersUnsorted = true;

		for (ModifierRecord modifierRecord : this.modifiers) {
			if (id.equals(modifierRecord.id)) {
				modifierRecord.setOrder(order);
			}
		}
	}

	@TestOnly
	void clearModifiers() {
		this.modifiers.clear();
		this.modifiersUnsorted = true;
	}

	private final Map<ModificationPhase, Map<Identifier, ModifierRecord>> identifiedModifiers = new HashMap<>();

	private List<ModifierRecord> getSortedModifiers() {
		if (this.modifiersUnsorted) {
			// Resort modifiers
			this.modifiers.sort(MODIFIER_ORDER_COMPARATOR);
			this.modifiers.removeIf(r -> r.canBeDropped);
			this.modifiersUnsorted = false;
		}

		return this.modifiers;
	}

	public void finalizeWorldGen(DynamicRegistryManager impl, WorldSaveProperties worldSaveProperties, ResourceManager resourceManager) {
		this.reloader.apply(resourceManager, impl);
		this.addMissingModifiers();
		this.updateIdentifiedModifiers();

		Stopwatch sw = Stopwatch.createStarted();

		// Now that we apply biome modifications inside the MinecraftServer constructor, we should only ever do
		// this once for a dynamic registry manager. Marking the dynamic registry manager as modified ensures a crash
		// if the precondition is violated.
		var modificationTracker = (BiomeModificationMarker) impl;
		modificationTracker.quilt$markModified();

		Registry<Biome> biomes = impl.get(RegistryKeys.BIOME);

		// Build a list of all biome keys in ascending order of their raw-id to get a consistent result in case
		// someone does something stupid.
		List<RegistryKey<Biome>> keys = biomes.getEntries().stream()
				.map(Map.Entry::getKey)
				.sorted(Comparator.comparingInt(key -> biomes.getRawId(biomes.getOrThrow(key))))
				.toList();

		List<ModifierRecord> sortedModifiers = this.getSortedModifiers();

		int biomesChanged = 0;
		int biomesProcessed = 0;
		int modifiersApplied = 0;

		for (RegistryKey<Biome> key : keys) {
			Biome biome = biomes.getOrThrow(key);

			biomesProcessed++;

			// Make a copy of the biome to allow selection contexts to see it unmodified,
			// But do so only once it's known anything wants to modify the biome at all
			var context = new BiomeSelectionContextImpl(impl, key, biome);
			BiomeModificationContextImpl modificationContext = null;

			for (ModifierRecord modifier : sortedModifiers) {
				if (modifier.canBeDropped) {
					continue;
				}

				if (modifier.selector.test(context)) {
					LOGGER.trace("Applying modifier {} to {}", modifier, key.getValue());

					// Create the copy only if at least one modifier applies, since it's pretty costly
					if (modificationContext == null) {
						biomesChanged++;
						modificationContext = new BiomeModificationContextImpl(impl, biome);
					}

					modifier.apply(context, modificationContext);
					modifiersApplied++;
				}
			}

			// Re-freeze and apply certain cleanup actions
			if (modificationContext != null) {
				modificationContext.freeze();
			}
		}

		if (biomesProcessed > 0) {
			LOGGER.info("Applied {} biome modifications to {} of {} new biomes in {}", modifiersApplied, biomesChanged,
					biomesProcessed, sw);
		}
	}

	private static class ModifierRecord {
		private final ModificationPhase phase;

		private final Identifier id;

		private final Predicate<BiomeSelectionContext> selector;

		private final BiConsumer<BiomeSelectionContext, BiomeModificationContext> contextSensitiveModifier;

		private final Consumer<BiomeModificationContext> modifier;

		// Whenever this is modified, the modifiers need to be resorted
		private int order;

		// Whenever this is true, the modifier will be dropped from the list on the next reorder
		private boolean canBeDropped = false;

		ModifierRecord(ModificationPhase phase, Identifier id, Predicate<BiomeSelectionContext> selector, Consumer<BiomeModificationContext> modifier) {
			this.phase = phase;
			this.id = id;
			this.selector = selector;
			this.modifier = modifier;
			this.contextSensitiveModifier = null;
		}

		ModifierRecord(ModificationPhase phase, Identifier id, Predicate<BiomeSelectionContext> selector, BiConsumer<BiomeSelectionContext, BiomeModificationContext> modifier) {
			this.phase = phase;
			this.id = id;
			this.selector = selector;
			this.contextSensitiveModifier = modifier;
			this.modifier = null;
		}

		ModifierRecord(ModificationPhase phase, Identifier id, Supplier<BiomeModifier> modifierFunction) {
			this.phase = phase;
			this.id = id;
			this.selector = ctx -> {
				var modifier = modifierFunction.get();
				if (modifier != null) {
					return modifier.shouldModify(ctx);
				}

				return false;
			};
			this.contextSensitiveModifier = (selectionCtx, modificationCtx) -> {
				var modifier = modifierFunction.get();
				if (modifier != null) {
					modifier.modify(selectionCtx, modificationCtx);
				}
			};
			this.modifier = null;
		}

		@Override
		public String toString() {
			if (this.modifier != null) {
				return this.modifier.toString();
			} else {
				return this.contextSensitiveModifier.toString();
			}
		}

		public void apply(BiomeSelectionContext context, BiomeModificationContextImpl modificationContext) {
			if (this.contextSensitiveModifier != null) {
				this.contextSensitiveModifier.accept(context, modificationContext);
			} else {
				this.modifier.accept(modificationContext);
			}
		}

		public void setOrder(int order) {
			this.order = order;
		}
	}
}
