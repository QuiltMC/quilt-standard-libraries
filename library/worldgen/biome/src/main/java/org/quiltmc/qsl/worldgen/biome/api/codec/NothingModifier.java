package org.quiltmc.qsl.worldgen.biome.api.codec;

import com.mojang.serialization.Codec;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.worldgen.biome.api.BiomeModificationContext;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifier;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectionContext;

public final class NothingModifier implements BiomeModifier {
	public static final Identifier IDENTIFIER = new Identifier("quilt", "nothing");
	public static final NothingModifier INSTANCE = new NothingModifier();
	public static final Codec<NothingModifier> CODEC = Codec.unit(INSTANCE);

	private NothingModifier() {
	}

	@Override
	public Identifier getCodecIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public boolean shouldModify(BiomeSelectionContext context) {
		return false;
	}

	@Override
	public void modify(BiomeSelectionContext selectionContext, BiomeModificationContext modificationContext) {

	}
}
