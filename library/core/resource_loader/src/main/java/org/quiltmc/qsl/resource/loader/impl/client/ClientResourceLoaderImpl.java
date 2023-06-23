/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.resource.loader.impl.client;

import org.jetbrains.annotations.ApiStatus;

import org.quiltmc.loader.api.minecraft.ClientOnly;

@ClientOnly
@ApiStatus.Internal
public final class ClientResourceLoaderImpl {
	/**
	 * Represents the reload context for client resource loading,
	 * a {@code null} value means there's no client resource loading on the current thread.
	 */
	private static final ThreadLocal<Boolean> RELOAD_CONTEXT = new ThreadLocal<>();

	private ClientResourceLoaderImpl() {
		throw new UnsupportedOperationException("ClientResourceLoaderImpl only contains static definitions.");
	}

	public static void pushReloadContext(boolean first) {
		RELOAD_CONTEXT.set(first);
	}

	public static Boolean getReloadContext() {
		return RELOAD_CONTEXT.get();
	}

	public static void popReloadContext() {
		RELOAD_CONTEXT.remove();
	}
}
