package qsl.internal;

/**
 * Version constants used across the convention build scripts.
 * <p>
 * To use inside of convention build scripts, simply import this class and refer to the public static final fields.
 */
public final class Versions {
	/*
	 * These must be in a Java class file due to issues with keeping this data in the groovy source set, since the
	 * convention plugins will not be able to see the groovy classes then.
	 *
	 * The gradle.properties does not work here either because you would need to load it in every single project, and it
	 * is not strictly defined in the IDE.
	 */

	/**
	 * The QSL version
	 */
	public static final String QSL_VERSION = "0.1.0";

	/**
	 * The target Minecraft version.
	 */
	public static final String MINECRAFT_VERSION = "22w06a";

	/**
	 * The target Quilt Mappings build.
	 */
	public static final int MAPPINGS_BUILD = 5;

	/**
	 * The version of Quilt Loader to use.
	 */
	public static final String LOADER_VERSION = "0.13.1";

	/**
	 * The target Java version.
	 */
	public static final int JAVA_VERSION = 17; // Minecraft is Java 17

	public static boolean isMinecraftVersionNonRelease() {
		return MINECRAFT_VERSION.matches("^\\d\\dw\\d\\d[a-z]$");
	}

	public static String getMinecraftVersionFancyString() {
		if (isMinecraftVersionNonRelease()) {
			return MINECRAFT_VERSION;
		}

		String[] version = MINECRAFT_VERSION.split("\\.");

		int index;
		if ((index = version[1].indexOf("-pre")) != -1 || (index = version[1].indexOf("-rc")) != -1) {
			version[1] = version[1].substring(0, index);
		}

		return version[0] + '.' + version[1];
	}

	private Versions() {
	}
}
