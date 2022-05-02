package org.quiltmc.qsl.key.binds.api;

public interface QuiltKeyBind {
	/**
	 * Gets whenever the key bind is from Vanilla or not.
	 * This is automatically determined by using GameOptions' allKeys property.
	 *
	 * @return {@code true} if the key bind is from Vanilla, {@code false} otherwise
	 */
	default boolean isVanilla() {
		return false;
	}
}
