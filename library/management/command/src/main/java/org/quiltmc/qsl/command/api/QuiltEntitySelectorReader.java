package org.quiltmc.qsl.command.api;

/**
 * An injected extension to {@link net.minecraft.command.EntitySelectorReader EntitySelectorReader}.
 *
 * <p>Allows mods to set and check arbitrary flags, useful for ensuring an entity selector option is only used once.</p>
 *
 * @see EntitySelectorOptionRegistrationCallback
 */
public interface QuiltEntitySelectorReader {
	/**
	 * Gets the value for a flag.
	 * @param key   the flag name
	 * @return		the corresponding value
	 */
	boolean getFlag(String key);

	/**
	 * Sets the value for a flag.
	 * @param key	the flag name
	 * @param value the value to set the flag to
	 */
	void setFlag(String key, boolean value);
}
