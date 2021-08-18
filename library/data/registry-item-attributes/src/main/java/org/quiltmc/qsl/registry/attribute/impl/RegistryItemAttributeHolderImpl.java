package org.quiltmc.qsl.registry.attribute.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import org.quiltmc.qsl.registry.attribute.api.RegistryItemAttribute;
import org.quiltmc.qsl.registry.attribute.api.RegistryItemAttributeHolder;
import net.minecraft.util.registry.Registry;
import java.util.Optional;

public class RegistryItemAttributeHolderImpl<R> implements RegistryItemAttributeHolder<R> {
	public static <R> RegistryItemAttributeHolder<R> getCombined(Registry<R> registry) {
		var builtin = getBuiltin(registry);
		var data = getData(registry);
		// data values override built-in values
		return new CombinedRegistryItemAttributeHolderImpl<>(ImmutableList.of(data, builtin));
	}

	@SuppressWarnings("unchecked")
	public static <R> BuiltinRegistryItemAttributeHolder<R> getBuiltin(Registry<R> registry) {
		var internals = (QuiltRegistryInternals) registry;
		var holder = internals.qsl$getBuiltinItemAttributeHolder();
		if (holder == null) {
			internals.qsl$setBuiltinItemAttributeHolder(holder = new BuiltinRegistryItemAttributeHolder<>());
		}
		return (BuiltinRegistryItemAttributeHolder<R>) holder;
	}

	@SuppressWarnings("unchecked")
	public static <R> BuiltinRegistryItemAttributeHolder<R> getData(Registry<R> registry) {
		var internals = (QuiltRegistryInternals) registry;
		var holder = internals.qsl$getItemAttributeHolder();
		if (holder == null) {
			internals.qsl$setItemAttributeHolder(holder = new BuiltinRegistryItemAttributeHolder<>());
		}
		return (BuiltinRegistryItemAttributeHolder<R>) holder;
	}

	protected final Table<R, RegistryItemAttribute<R, ?>, Object> valueTable;

	@SuppressWarnings("UnstableApiUsage")
	protected RegistryItemAttributeHolderImpl() {
		valueTable = Tables.newCustomTable(new Reference2ObjectOpenHashMap<>(), Object2ObjectOpenHashMap::new);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> getValue(R item, RegistryItemAttribute<R, T> attribute) {
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

	public <T> void putValue(R item, RegistryItemAttribute<R, T> attribute, T value) {
		valueTable.put(item, attribute, value);
	}

	public void clear() {
		valueTable.clear();
	}
}
