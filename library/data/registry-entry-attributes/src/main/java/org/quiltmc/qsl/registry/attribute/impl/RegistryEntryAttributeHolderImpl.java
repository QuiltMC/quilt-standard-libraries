package org.quiltmc.qsl.registry.attribute.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttribute;
import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttributeHolder;
import net.minecraft.util.registry.Registry;
import java.util.Optional;

public class RegistryEntryAttributeHolderImpl<R> implements RegistryEntryAttributeHolder<R> {
	public static <R> RegistryEntryAttributeHolder<R> getCombined(Registry<R> registry) {
		var builtin = getBuiltin(registry);
		var data = getData(registry);
		// data values override built-in values
		return new CombinedRegistryEntryAttributeHolderImpl<>(ImmutableList.of(data, builtin));
	}

	@SuppressWarnings("unchecked")
	public static <R> BuiltinRegistryEntryAttributeHolder<R> getBuiltin(Registry<R> registry) {
		var internals = (QuiltRegistryInternals) registry;
		var holder = internals.qsl$getBuiltinAttributeHolder();
		if (holder == null) {
			internals.qsl$setBuiltinAttributeHolder(holder = new BuiltinRegistryEntryAttributeHolder<>());
		}
		return (BuiltinRegistryEntryAttributeHolder<R>) holder;
	}

	@SuppressWarnings("unchecked")
	public static <R> BuiltinRegistryEntryAttributeHolder<R> getData(Registry<R> registry) {
		var internals = (QuiltRegistryInternals) registry;
		var holder = internals.qsl$getDataAttributeHolder();
		if (holder == null) {
			internals.qsl$setDataAttributeHolder(holder = new BuiltinRegistryEntryAttributeHolder<>());
		}
		return (BuiltinRegistryEntryAttributeHolder<R>) holder;
	}

	protected final Table<R, RegistryEntryAttribute<R, ?>, Object> valueTable;

	@SuppressWarnings("UnstableApiUsage")
	protected RegistryEntryAttributeHolderImpl() {
		valueTable = Tables.newCustomTable(new Reference2ObjectOpenHashMap<>(), Object2ObjectOpenHashMap::new);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> getValue(R item, RegistryEntryAttribute<R, T> attribute) {
		var itemRow = valueTable.row(item);
		if (itemRow == null) {
			return Optional.ofNullable(attribute.getDefaultValue());
		}
		var rawValue = itemRow.get(attribute);
		if (rawValue == null) {
			return Optional.ofNullable(attribute.getDefaultValue());
		} else {
			return Optional.of((T) rawValue);
		}
	}

	public <T> void putValue(R item, RegistryEntryAttribute<R, T> attribute, T value) {
		valueTable.put(item, attribute, value);
	}

	public void clear() {
		valueTable.clear();
	}
}
