package org.quiltmc.qsl.command.mixin;

import net.minecraft.command.EntitySelectorReader;
import org.quiltmc.qsl.command.api.QuiltEntitySelectorReader;
import org.spongepowered.asm.mixin.Mixin;

import java.util.HashSet;
import java.util.Set;

@Mixin(EntitySelectorReader.class)
public class EntitySelectorReaderMixin implements QuiltEntitySelectorReader {
	private final Set<String> flags = new HashSet<>();

	@Override
	public boolean getFlag(String key) {
		return flags.contains(key);
	}

	@Override
	public void setFlag(String key, boolean value) {
		if (value) {
			flags.add(key);
		} else {
			flags.remove(key);
		}
	}
}
