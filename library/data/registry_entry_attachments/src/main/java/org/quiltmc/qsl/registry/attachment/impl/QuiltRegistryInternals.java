/*
 * Copyright 2021 QuiltMC
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

import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;

@ApiStatus.Internal
public interface QuiltRegistryInternals<R> {
	void quilt$registerAttachment(RegistryEntryAttachment<R, ?> attachment);

	@Nullable RegistryEntryAttachment<R, ?> quilt$getAttachment(Identifier id);

	Set<Map.Entry<Identifier, RegistryEntryAttachment<R, ?>>> quilt$getAttachmentEntries();

	RegistryEntryAttachmentHolder<R> quilt$getBuiltinAttachmentHolder();

	void quilt$setBuiltinAttachmentHolder(RegistryEntryAttachmentHolder<R> holder);

	RegistryEntryAttachmentHolder<R> quilt$getDataAttachmentHolder();

	void quilt$setDataAttachmentHolder(RegistryEntryAttachmentHolder<R> holder);

	RegistryEntryAttachmentHolder<R> quilt$getAssetsAttachmentHolder();

	void quilt$setAssetsAttachmentHolder(RegistryEntryAttachmentHolder<R> holder);
}
