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

package org.quiltmc.qsl.registry.attachment.mixin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;
import org.quiltmc.qsl.registry.attachment.impl.QuiltRegistryInternals;
import org.quiltmc.qsl.registry.attachment.impl.RegistryEntryAttachmentHolder;

@Mixin(Registry.class)
public abstract class RegistryMixin<R> implements QuiltRegistryInternals<R> {
	@Unique
	private final Map<Identifier, RegistryEntryAttachment<R, ?>> quilt$attachments = new HashMap<>();
	@Unique
	private RegistryEntryAttachmentHolder<R> quilt$builtinAttachmentHolder;
	@Unique
	private RegistryEntryAttachmentHolder<R> quilt$dataAttachmentHolder;
	@Unique
	private RegistryEntryAttachmentHolder<R> quilt$assetsAttachmentHolder;

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
	public RegistryEntryAttachmentHolder<R> quilt$getBuiltinAttachmentHolder() {
		return this.quilt$builtinAttachmentHolder;
	}

	@Override
	public void quilt$setBuiltinAttachmentHolder(RegistryEntryAttachmentHolder<R> holder) {
		this.quilt$builtinAttachmentHolder = holder;
	}

	@Override
	public RegistryEntryAttachmentHolder<R> quilt$getDataAttachmentHolder() {
		return this.quilt$dataAttachmentHolder;
	}

	@Override
	public void quilt$setDataAttachmentHolder(RegistryEntryAttachmentHolder<R> holder) {
		this.quilt$dataAttachmentHolder = holder;
	}

	@Override
	public RegistryEntryAttachmentHolder<R> quilt$getAssetsAttachmentHolder() {
		return this.quilt$assetsAttachmentHolder;
	}

	@Override
	public void quilt$setAssetsAttachmentHolder(RegistryEntryAttachmentHolder<R> holder) {
		this.quilt$assetsAttachmentHolder = holder;
	}
}
