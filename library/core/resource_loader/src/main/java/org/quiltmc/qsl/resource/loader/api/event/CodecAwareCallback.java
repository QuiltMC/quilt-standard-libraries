package org.quiltmc.qsl.resource.loader.api.event;

import com.mojang.serialization.Codec;

public interface CodecAwareCallback<SELF> {

	default Codec<? extends SELF> getCodec() {
		return null;
	}
}
