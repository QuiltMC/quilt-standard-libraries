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

package org.quiltmc.qsl.lifecycle.test.event.client;

import net.minecraft.client.MinecraftClient;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents;
import org.quiltmc.qsl.lifecycle.test.event.ServerLifecycleTests;

@ClientOnly
public final class ClientLifecycleTests implements ClientLifecycleEvents.Ready, ClientLifecycleEvents.Stopping {
	private boolean startCalled;
	private boolean stopCalled;

	@Override
	public void readyClient(MinecraftClient client) {
		if (this.startCalled) {
			throw new IllegalStateException("Start was already called!");
		}

		this.startCalled = true;
		client.submitAndJoin(() -> { // This should fail if the client thread was not bound yet.
			ServerLifecycleTests.LOGGER.info("Started the client");
		});
	}

	@Override
	public void stoppingClient(MinecraftClient client) {
		if (this.stopCalled) {
			throw new IllegalStateException("Stop was already called!");
		}

		this.stopCalled = true;
		ServerLifecycleTests.LOGGER.info("Client has started stopping!");
	}
}
