package qsl.internal;

/**
 * Version constants used across the convention build scripts.
 *
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
	public static final String MINECRAFT_VERSION = "1.17.1";

	/**
	 * The target Yarn build.
	 */
	public static final String YARN_BUILD = "2";

	/**
	 * The version of Quilt Loader to use.
	 */
	public static final String LOADER_VERSION = "0.13.1-rc.10";

	private Versions() {
	}
}
