package org.quiltmc.qsl.item.extensions.test;

import net.minecraft.item.Item;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class TransitiveAccessWidenerTest implements ModInitializer {
	public static final Item MODDED_MUSIC_DISC = new MusicDiscItem(
		5, SoundEvents.BLOCK_COMPOSTER_READY, new Item.Settings().maxCount(1), 4
	);

	@Override
	public void onInitialize(ModContainer mod) {
		// Registers a custom music disc, which is not possible without an access widener.
		// Do note that duplicate music discs of a same sound will have problems.
		Registry.register(Registries.ITEM, new Identifier(mod.metadata().id(), "modded_music_disc"), MODDED_MUSIC_DISC);
	}
}
