package org.quiltmc.qsl.base.api.event.data;

import net.minecraft.util.Identifier;

public interface CodecAwareCallback {

	default Identifier getCodecIdentifier() {
		return null;
	}
}
