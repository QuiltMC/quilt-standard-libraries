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
