/*
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

package org.quiltmc.qsl.registry.attachment.impl;

import net.minecraft.resource.ResourceType;

public final class DataRegistryEntryAttachmentHolder<R> extends RegistryEntryAttachmentHolder<R> {
	DataRegistryEntryAttachmentHolder() {}

	/**
	 * Removes all attachment values that are loaded from the specified source.
	 *
	 * @param source source that we're preparing to reload
	 */
	public void prepareReloadSource(ResourceType source) {
		this.valueTagTable.rowKeySet().removeIf(attach -> attach.side().shouldLoad(source));
		this.valueTable.rowKeySet().removeIf(attach -> attach.side().shouldLoad(source));
	}
}
