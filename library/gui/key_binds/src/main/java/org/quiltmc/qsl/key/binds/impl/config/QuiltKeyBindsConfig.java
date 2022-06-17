package org.quiltmc.qsl.key.binds.impl.config;

import org.quiltmc.config.api.WrappedConfig;
import org.quiltmc.config.api.values.ValueList;
import org.quiltmc.config.api.values.ValueMap;

public class QuiltKeyBindsConfig extends WrappedConfig {
	public final boolean show_tutorial_toast = true;
	public final ValueMap<ValueList<String>> key_binds = ValueMap.builder(ValueList.create("")).build();
	public final ValueMap<ValueList<String>> unused_key_binds = ValueMap.builder(ValueList.create("")).build();
}
