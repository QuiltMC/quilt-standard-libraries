package org.quiltmc.qsl.worldgen.biome.api;

import org.quiltmc.qsl.base.api.event.data.CodecAwareCallback;
import org.quiltmc.qsl.base.api.event.data.CodecMap;

public interface BiomeModifier extends CodecAwareCallback {
	CodecMap<BiomeModifier> BIOME_MODIFIER_CODECS = new CodecMap<>();

	boolean shouldModify(BiomeSelectionContext context);

	void modify(BiomeSelectionContext selectionContext, BiomeModificationContext modificationContext);
}
