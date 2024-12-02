package qsl.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Version constants used across the convention build scripts.
 * <p>
 * To use inside of convention build scripts, simply import this class and refer to the public static final fields.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
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
	// Note: Make sure this matches QFAPI's gradle.properties entry for qsl_version
	public static final String QSL_VERSION = "10.0.0-alpha.4";

	/**
	 * The target Minecraft version.
	 */
	public static final MinecraftVersion MINECRAFT_VERSION = new MinecraftVersion("1.21.1");

	/**
	 * The Minecraft versions this version of QSL is compatible with.
	 */
	public static final List<MinecraftVersion> COMPATIBLE_VERSIONS = versions("1.21");

	/**
	 * The target Quilt Mappings build.
	 */
	public static final int MAPPINGS_BUILD = 2;

	/**
	 * The version of Quilt Loader to use.
	 */
	public static final String LOADER_VERSION = "0.25.0";

	/**
	 * The target Java version.
	 */
	public static final int JAVA_VERSION = 21; // Minecraft is Java 21

	//region 3rd-parties libraries/mods to test
	/**
	 * The version of Databreaker to use in the no-op DFU testmod.
	 */
	public static Optional<String> DATABREAKER_VERSION = Optional.empty(); //of("0.2.10");

	/**
	 * The version of LazyDFU to use in the DFU testmods.
	 */
	public static Optional<String> LAZYDFU_VERSION = Optional.empty(); //of("0.1.3");
	//endregion

	private Versions() {}

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
