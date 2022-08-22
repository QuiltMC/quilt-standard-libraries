package qsl.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	public static final String QSL_VERSION = "3.0.0-beta.14";

	/**
	 * The target Minecraft version.
	 */
	public static final MinecraftVersion MINECRAFT_VERSION = new MinecraftVersion("1.19.2");

	/**
	 * The Minecraft versions this version of QSL is compatible with.
	 */
	public static final List<MinecraftVersion> COMPATIBLE_VERSIONS = versions(new MinecraftVersion("1.19.1"));

	/**
	 * The target Quilt Mappings build.
	 */
	public static final int MAPPINGS_BUILD = 3;

	/**
	 * The version of Quilt Loader to use.
	 */
	public static final String LOADER_VERSION = "0.17.3";

	/**
	 * The target Java version.
	 */
	public static final int JAVA_VERSION = 17; // Minecraft is Java 17

	private Versions() {
	}

	private static List<MinecraftVersion> versions(Object... versions) {
		var list = new ArrayList<MinecraftVersion>();

		for (var version : versions) {
			if (version instanceof String name) {
				list.add(new MinecraftVersion(name, MINECRAFT_VERSION.versionEdition()));
			} else if (version instanceof MinecraftVersion mcVersion) {
				list.add(mcVersion);
			} else {
				throw new IllegalArgumentException("Unexpected version \"" + version + "\", only String and MinecraftVersion are accepted.");
			}
		}

		return Collections.unmodifiableList(list);
	}
}
