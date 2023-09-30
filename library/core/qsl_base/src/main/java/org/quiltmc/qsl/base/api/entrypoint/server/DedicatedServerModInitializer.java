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

package org.quiltmc.qsl.base.api.entrypoint.server;

import org.quiltmc.loader.api.ModContainer;

/**
 * A mod initializer which is run only on {@link net.fabricmc.api.EnvType#SERVER}.
 * <p>
 * In {@code quilt.mod.json}, the entrypoint is defined with {@value #ENTRYPOINT_KEY} key.
 * <p>
 * Currently, it is executed in {@link net.minecraft.server.Main#main(String[])}, just after the EULA has been agreed to.
 *
 * @see org.quiltmc.qsl.base.api.entrypoint.ModInitializer
 * @see org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer
 */
public interface DedicatedServerModInitializer {
	/**
	 * Represents the key which this entrypoint is defined with, whose value is {@value}.
	 */
	String ENTRYPOINT_KEY = "server_init";

	/**
	 * Runs the mod initializer on the dedicated server environment.
	 *
	 * @param mod the mod which is initialized
	 */
	void onInitializeServer(ModContainer mod);
}
