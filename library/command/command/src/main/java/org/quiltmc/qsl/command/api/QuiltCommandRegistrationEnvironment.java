package org.quiltmc.qsl.command.api;

import org.jetbrains.annotations.ApiStatus;

/**
 * Represents an extension to the {@link net.minecraft.server.command.CommandManager.RegistrationEnvironment} enum,
 * and is automatically injected into it.
 */
@ApiStatus.NonExtendable
public interface QuiltCommandRegistrationEnvironment {
	/**
	 * {@return {@code true} if the environment corresponds to the dedicated server, otherwise {@code false}}
	 */
	boolean isDedicated();

	/**
	 * {@return {@code true} if the environment corresponds to single-player, otherwise {@code false}}
	 */
	boolean isIntegrated();
}
