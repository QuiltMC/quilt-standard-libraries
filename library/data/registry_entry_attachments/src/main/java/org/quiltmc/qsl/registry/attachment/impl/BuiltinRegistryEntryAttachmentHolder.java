package org.quiltmc.qsl.registry.attachment.impl;

import java.util.Objects;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;

public final class BuiltinRegistryEntryAttachmentHolder<R> extends RegistryEntryAttachmentHolder<R> {
	public static final int FLAG_NONE = 0;
	public static final int FLAG_COMPUTED = 1;

	public final Table<RegistryEntryAttachment<R, ?>, R, Integer> valueFlagTable;

	@SuppressWarnings("UnstableApiUsage")
	BuiltinRegistryEntryAttachmentHolder() {
		this.valueFlagTable = Tables.newCustomTable(new Object2ReferenceOpenHashMap<>(), Reference2IntOpenHashMap::new);
	}

	public int getValueFlags(RegistryEntryAttachment<R, ?> attachment, R entry) {
		return Objects.requireNonNullElse(this.valueFlagTable.get(attachment, entry), FLAG_NONE);
	}

	public boolean isValueComputed(RegistryEntryAttachment<R, ?> attachment, R entry) {
		return (getValueFlags(attachment, entry) & FLAG_COMPUTED) == FLAG_COMPUTED;
	}

	public <T> void putValue(RegistryEntryAttachment<R, T> attachment, R entry, T value, int flags) {
		this.valueTable.put(attachment, entry, value);
		this.valueFlagTable.put(attachment, entry, flags);
	}

	@Override
	protected <T> void putValueFromTag(RegistryEntryAttachment<R, T> attachment, R entry, T value) {
		putValue(attachment, entry, value, FLAG_COMPUTED);
	}

	@Override
	public void clear() {
		super.clear();
		this.valueFlagTable.clear();
	}
}
