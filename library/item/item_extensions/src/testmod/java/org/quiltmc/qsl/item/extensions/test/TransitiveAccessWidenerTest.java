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

package org.quiltmc.qsl.item.extensions.test;

import net.minecraft.item.Item;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class TransitiveAccessWidenerTest implements ModInitializer {
	public static final Item MODDED_MUSIC_DISC = new MusicDiscItem(
			5, SoundEvents.BLOCK_COMPOSTER_READY, new Item.Settings().maxCount(1).rarity(Rarity.RARE), 4
	);

	@Override
	public void onInitialize(ModContainer mod) {
		// Registers a custom music disc, which is not possible without an access widener.
		// Do note that duplicate music discs of a same sound will have problems.
		Registry.register(Registries.ITEM, new Identifier(mod.metadata().id(), "modded_music_disc"), MODDED_MUSIC_DISC);
	}
}
