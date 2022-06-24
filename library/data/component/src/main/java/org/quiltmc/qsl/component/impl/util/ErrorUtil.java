package org.quiltmc.qsl.component.impl.util;

import java.util.function.Supplier;

public class ErrorUtil {
	public static Supplier<IllegalArgumentException> illegalArgument(String message) {
		return () -> new IllegalArgumentException(message);
	}

	public static Supplier<IllegalStateException> illegalState(String message) {
		return () -> new IllegalStateException(message);
	}
}
