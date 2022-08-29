/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.registry.impl.sync;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.util.registry.Registry;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

@ApiStatus.Internal
public class RegistrySyncInitializer implements ModInitializer {
	@Override
	public void onInitialize(ModContainer mod) {
		ServerRegistrySync.readConfig();

		SynchronizedRegistry.markForSync(
				Registry.BLOCK,
				Registry.BLOCK_ENTITY_TYPE,
				Registry.CAT_VARIANT,
				Registry.COMMAND_ARGUMENT_TYPE,
				Registry.ENCHANTMENT,
				Registry.ENTITY_TYPE,
				Registry.FLUID,
				Registry.FROG_VARIANT,
				Registry.GAME_EVENT,
				Registry.ITEM,
				Registry.PAINTING_VARIANT,
				Registry.PARTICLE_TYPE,
				Registry.SCREEN_HANDLER,
				Registry.SOUND_EVENT,
				Registry.STAT_TYPE,
				Registry.STATUS_EFFECT,
				Registry.VILLAGER_TYPE,
				Registry.VILLAGER_PROFESSION
		);
	}
}
