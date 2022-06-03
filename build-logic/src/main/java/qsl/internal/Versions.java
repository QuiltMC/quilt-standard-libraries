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
	public static final String QSL_VERSION = "2.0.0-beta.1";

	/**
	 * The target Minecraft version.
	 */
	public static final MinecraftVersion MINECRAFT_VERSION = new MinecraftVersion("22w14a", "1.19");

	/**
	 * The target Quilt Mappings build.
	 */
	public static final int MAPPINGS_BUILD = 2;

	/**
	 * The version of Quilt Loader to use.
	 */
	public static final String LOADER_VERSION = "0.16.0";

	/**
	 * The target Java version.
	 */
	public static final int JAVA_VERSION = 17; // Minecraft is Java 17

	private Versions() {
	}
}
