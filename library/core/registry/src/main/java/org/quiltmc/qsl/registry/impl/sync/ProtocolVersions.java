package org.quiltmc.qsl.registry.impl.sync;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.stream.IntStream;

public class ProtocolVersions {
	// Delete this for 1.20
	public static final int EXT_3 = 3;
	public static final int CURRENT_VERSION = 3;
	public static final int OLDEST_SUPPORTED_VERSION = 2;
	public static final IntSet IMPL_SUPPORTED_VERSIONS = IntSet.of(IntStream.rangeClosed(OLDEST_SUPPORTED_VERSION, CURRENT_VERSION).toArray());

	public static final int NO_PROTOCOL = -1;
	public static final int FAPI_PROTOCOL = -2;


	public static int getHighestSupportedLocal(IntList supportedRemote) {
		return getHighestSupported(IMPL_SUPPORTED_VERSIONS, supportedRemote);
	}

	public static int getHighestSupported(IntCollection supportedLocal, IntList supportedRemote) {
		int highestSupported = NO_PROTOCOL;

		for (var i = 0; i < supportedRemote.size(); i++) {
			int version = supportedRemote.getInt(i);

			if (version > highestSupported && supportedLocal.contains(version)) {
				highestSupported = version;
			}
		}

		return highestSupported;
	}
}
