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

package org.quiltmc.qsl.networking.impl.server;

import java.util.function.Consumer;

import net.minecraft.network.configuration.ConfigurationTask;
import net.minecraft.network.packet.Packet;

public class SendChannelsTask implements ConfigurationTask {
	public static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type("qsl:send_channels");
	private final ServerConfigurationNetworkAddon addon;

	public SendChannelsTask(ServerConfigurationNetworkAddon addon) {
		this.addon = addon;
	}

	@Override
	public void start(Consumer<Packet<?>> task) {
		this.addon.onConfigureReady();
	}

	@Override
	public Type getType() {
		return TYPE;
	}
}
