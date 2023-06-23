/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.registry.attachment.mixin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;
import org.quiltmc.qsl.registry.attachment.impl.BuiltinRegistryEntryAttachmentHolder;
import org.quiltmc.qsl.registry.attachment.impl.DataRegistryEntryAttachmentHolder;
import org.quiltmc.qsl.registry.attachment.impl.QuiltRegistryInternals;

@Mixin(RegistryKey.class)
public abstract class RegistryKeyMixin<R> implements QuiltRegistryInternals<R> {
	@Unique
	private final Map<Identifier, RegistryEntryAttachment<R, ?>> quilt$attachments = new HashMap<>();
	@Unique
	private BuiltinRegistryEntryAttachmentHolder<R> quilt$builtinAttachmentHolder;
	@Unique
	private DataRegistryEntryAttachmentHolder<R> quilt$dataAttachmentHolder;

	@Override
	public void quilt$registerAttachment(RegistryEntryAttachment<R, ?> attachment) {
		this.quilt$attachments.put(attachment.id(), attachment);
	}

	@Override
	public @Nullable RegistryEntryAttachment<R, ?> quilt$getAttachment(Identifier id) {
		return this.quilt$attachments.get(id);
	}

	@Override
	public Set<Map.Entry<Identifier, RegistryEntryAttachment<R, ?>>> quilt$getAttachmentEntries() {
		return this.quilt$attachments.entrySet();
	}

	@Override
	public BuiltinRegistryEntryAttachmentHolder<R> quilt$getBuiltinAttachmentHolder() {
		return this.quilt$builtinAttachmentHolder;
	}

	@Override
	public void quilt$setBuiltinAttachmentHolder(BuiltinRegistryEntryAttachmentHolder<R> holder) {
		this.quilt$builtinAttachmentHolder = holder;
	}

	@Override
	public DataRegistryEntryAttachmentHolder<R> quilt$getDataAttachmentHolder() {
		return this.quilt$dataAttachmentHolder;
	}

	@Override
	public void quilt$setDataAttachmentHolder(DataRegistryEntryAttachmentHolder<R> holder) {
		this.quilt$dataAttachmentHolder = holder;
	}
}
