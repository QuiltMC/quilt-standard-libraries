package org.quiltmc.qsl.base.api.event.data;

import net.minecraft.util.Identifier;

/**
 * Marks an object that may be aware of a codec that can be used to encode it. Implementing objects should override
 * {@link #getCodecIdentifier()} to return the identifier of the codec if they are encodable, or leave the default
 * implementation if they are not. Identifiers provided should correspond to the identifiers of codecs registered in
 * some {@link CodecMap}.
 */
public interface CodecAware {

	/**
	 * {@return the identifier of the codec that can be used to encode this object, or {@code null} if this object is not encodable}
	 */
	default Identifier getCodecIdentifier() {
		return null;
	}
}
