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

package org.quiltmc.qsl.base.api.entrypoint;

import net.minecraft.Bootstrap;

import org.quiltmc.loader.api.ModContainer;

/**
 * A mod initializer.
 * <p>
 * In {@code quilt.mod.json}, the entrypoint is defined with {@value #ENTRYPOINT_KEY} key.
 * <p>
 * Currently, it is executed in {@link Bootstrap#initialize()}, just before the freezing of built-in registries.
 *
 * @see org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer
 * @see org.quiltmc.qsl.base.api.entrypoint.server.DedicatedServerModInitializer
 */
public interface ModInitializer {
	/**
	 * Represents the key which this entrypoint is defined with, whose value is {@value}.
	 */
	String ENTRYPOINT_KEY = "init";

	/**
	 * Runs the mod initializer.
	 *
	 * @param mod the mod which is initialized
	 */
	void onInitialize(ModContainer mod);
}
