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

package org.quiltmc.qsl.registry.attachment.impl;

import org.jetbrains.annotations.ApiStatus;

import org.quiltmc.loader.api.minecraft.ClientOnly;

/**
 * Simple guard class that prevents access to client-sided attachments in a dedicated server environment.
 */
@ApiStatus.Internal
public final class ClientSideGuard {
	private static boolean allowed = false;

	@ClientOnly
	public static void setAccessAllowed() {
		allowed = true;
	}

	public static boolean isAccessAllowed() {
		return allowed;
	}

	public static void assertAccessAllowed() {
		if (!allowed) {
			throw new IllegalStateException("Access to client-sided registry attachments is not allowed here!");
		}
	}
}
