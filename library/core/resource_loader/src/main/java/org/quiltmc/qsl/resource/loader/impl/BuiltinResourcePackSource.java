package org.quiltmc.qsl.resource.loader.impl;

import net.minecraft.resource.pack.ResourcePackSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Represents a built-in resource pack source.
 * Similar to {@link ResourcePackSource#PACK_SOURCE_BUILTIN} but specifies the mod name too.
 */
public class BuiltinResourcePackSource implements ResourcePackSource {
	private static final Text SOURCE_BUILTIN_TEXT = Text.translatable("pack.source.builtin");
	private final ModNioResourcePack pack;
	private final Text text;
	private final Text tooltip;

	BuiltinResourcePackSource(ModNioResourcePack pack) {
		String modName = pack.modInfo.name();

		if (modName == null) {
			modName = pack.modInfo.id();
		}

		this.pack = pack;
		this.text = SOURCE_BUILTIN_TEXT;
		this.tooltip = Text.translatable("options.generic_value", SOURCE_BUILTIN_TEXT, modName);
	}

	@Override
	public Text decorate(Text description) {
		return Text.translatable("pack.nameAndSource", description, this.text).formatted(Formatting.GRAY);
	}

	@Override
	public boolean shouldAddAutomatically() {
		return this.pack.getActivationType().isEnabledByDefault();
	}

	public Text getTooltip() {
		return this.tooltip;
	}
}
