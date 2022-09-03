package org.quiltmc.qsl.command.api;

public interface QuiltEntitySelectorReader {
	boolean getFlag(String key);
	void setFlag(String key, boolean value);
}
