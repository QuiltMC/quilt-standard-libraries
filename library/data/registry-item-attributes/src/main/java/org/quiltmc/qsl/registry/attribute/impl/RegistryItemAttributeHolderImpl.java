package org.quiltmc.qsl.registry.attribute.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import org.quiltmc.qsl.registry.attribute.api.RegistryItemAttribute;
import org.quiltmc.qsl.registry.attribute.api.RegistryItemAttributeHolder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import java.util.*;

public final class RegistryItemAttributeHolderImpl<R> implements RegistryItemAttributeHolder<R> {
	@SuppressWarnings("unchecked")
	public static <R> RegistryItemAttributeHolderImpl<R> get(Registry<R> registry) {
		var internals = (QuiltRegistryInternals) registry;
		var holder = internals.qsl$getItemAttributeHolder();
		if (holder == null) {
			internals.qsl$setItemAttributeHolder(holder = new RegistryItemAttributeHolderImpl<>());
		}
		return (RegistryItemAttributeHolderImpl<R>) holder;
	}

	private final HashMap<Identifier, RegistryItemAttribute<R, ?>> attributes;
	private final Table<R, RegistryItemAttribute<R, ?>, Object> valueTable;

	@SuppressWarnings("UnstableApiUsage")
	private RegistryItemAttributeHolderImpl() {
		attributes = new HashMap<>();
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

	public <T> void addAttribute(RegistryItemAttribute<R, T> attribute) {
		attributes.put(attribute.getId(), attribute);
	}

	public Set<Map.Entry<Identifier, RegistryItemAttribute<R, ?>>> getAttributes() {
		return attributes.entrySet();
	}
}
