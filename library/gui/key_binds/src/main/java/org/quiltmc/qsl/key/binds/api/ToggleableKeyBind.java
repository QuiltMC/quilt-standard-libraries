package org.quiltmc.qsl.key.binds.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.option.KeyBind;

import org.quiltmc.qsl.base.api.util.InjectedInterface;

// TODO - Add Javadocs; You can nab the ones from KeyBindRegistry
@Environment(EnvType.CLIENT)
@InjectedInterface(KeyBind.class)
public interface ToggleableKeyBind {
	/**
	 * Gets whenever the key bind is enabled or not.
	 *
	 * @return {@code true} if the key bind is enabled, {@code false} otherwise
	 */
	default boolean isEnabled() {
		return true;
	}

	/**
	 * Gets whenever the key bind is disabled or not.
	 *
	 * @return {@code true} if the key bind is disabled, {@code false} otherwise
	 */
	default boolean isDisabled() {
		return false;
	}

	/**
	 * Enables the key bind.
	 *
	 * <p>If the key bind has been disabled more than once, this method will only
	 * decrement its internal counter instead of enabling the key bind.
	 */
	default void enable() { }

	/**
	 * Disables the key bind.
	 *
	 * <p>When a key bind is disabled, it is effectively hidden from the game,
	 * being non-existent to it. config/quilt/key_binds.json, however, will
	 * still remember the key bind's bound keys, similar to non-existent key binds.
	 *
	 * <p>If the key bind is disabled while already disabled, it will be increment
	 * an internal counter, making the next enable only decrement it instead of
	 * enabling the key bind.
	 */
	default void disable() { }
}
