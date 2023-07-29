package qsl.internal.extension;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.Nullable;
import qsl.internal.dependency.QslLibraryDependency;

/**
 * The definition of a QSL module within its buildscript.
 */
public interface QslModuleExtension {
	/**
	 * The name of this module, used for maven and moduleDependencies.
	 */
	Property<String> getModuleName();

	@Internal
	String getLibrary();

	/**
	 * The display name of this module, e.g. "Quilt Block Extensions API"
	 */
	Property<String> getName();

	/**
	 * The mod id of this module. Usually "quilt" + the module name
	 */
	Property<String> getId();

	/**
	 * A one-sentence description of what this module does.
	 */
	Property<String> getDescription();

	/**
	 * Adds an access widener to this module, which must be named "$MOD_ID.accesswidener" at the root of the module's
	 * resources directory.
	 */
	void accessWidener();

	/**
	 * Disables expecting "$MOD_ID.mixins.json" to be present in the resources directory
	 */
	void noMixins();

	/**
	 * Configures this module's dependencies on other modules.
	 * See the "Gradle Conventions" section of CONTRIBUTING.MD for more information.
	 */
	void moduleDependencies(Action<NamedDomainObjectContainer<QslLibraryDependency>> action);

	/**
	 * Makes the module provides another mod.
	 *
	 * @param id the mod identifier to provide
	 * @see #provides(String, String)
	 */
	default void provides(String id) {
		this.provides(id, null);
	}

	/**
	 * Makes the module provides another mod with a specific version.
	 *
	 * @param id      the mod identifier to provide
	 * @param version the mod version to provide
	 * @see #provides(String)
	 */
	void provides(String id, @Nullable String version);

	/**
	 * Configures the entrypoints for this module.
	 * See the "Gradle Conventions" section of CONTRIBUTING.MD for more information
	 */
	void entrypoints(Action<NamedDomainObjectContainer<QslModuleExtensionImpl.NamedWriteOnlyList>> action);

	void injectedInterface(String minecraftClass, Action<QslModuleExtensionImpl.NamedWriteOnlyList> action);

	/**
	 * Makes Loader only load this mod on the physical client.
	 */
	void clientOnly();

	/**
	 * Makes Loader only load this mod on the physical, dedicated server. This is almost never needed.
	 */
	void dedicatedServerOnly();
}
