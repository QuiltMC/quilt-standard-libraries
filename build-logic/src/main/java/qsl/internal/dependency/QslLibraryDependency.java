package qsl.internal.dependency;

import java.io.Serial;
import java.io.Serializable;

import org.gradle.api.Named;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;
import org.jetbrains.annotations.NotNull;

public class QslLibraryDependency implements Named, Serializable {
	@Serial
	// increment when changing this class to properly invalidate the generateQmj task
	private static final long serialVersionUID = 1L;
	@Input
	private final String name;
	@Input
	private final ListProperty<ModuleDependencyInfo> dependencyInfo;

	public QslLibraryDependency(ObjectFactory factory, String name) {
		this.dependencyInfo = factory.listProperty(ModuleDependencyInfo.class);
		this.name = name;
	}

	private void add(String module, ConfigurationType type) {
		this.dependencyInfo.add(new ModuleDependencyInfo(module, type));
	}

	public void api(String module) {
		this.add(module, ConfigurationType.API);
	}

	public void impl(String module) {
		this.add(module, ConfigurationType.IMPLEMENTATION);
	}

	public void testmodOnly(String module) {
		this.add(module, ConfigurationType.TESTMOD);
	}

	public void compileOnly(String module) {
		this.add(module, ConfigurationType.COMPILE_ONLY);
	}

	public ListProperty<ModuleDependencyInfo> getDependencyInfo() {
		return this.dependencyInfo;
	}

	@Override
	public @NotNull String getName() {
		return this.name;
	}

	public record ModuleDependencyInfo(String module, ConfigurationType type) implements Serializable {
		@Serial
		// increment when changing this class to properly invalidate the generateQmj task
		private static final long serialVersionUID = 1L;
	}

	/**
	 * The configuration type for the module dependency
	 */
	public enum ConfigurationType implements Serializable {
		API(JavaPlugin.API_CONFIGURATION_NAME, true),
		IMPLEMENTATION(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, true),
		TESTMOD("testmodImplementation", false),
		COMPILE_ONLY("compileOnly", false),
		RUNTIME_ONLY(JavaPlugin.RUNTIME_ONLY_CONFIGURATION_NAME, true);

		@Serial
		// increment when changing this class to properly invalidate the generateQmj task
		private static final long serialVersionUID = 2L;
		private final String configurationName;
		private final boolean isTransitive;

		ConfigurationType(String configurationName, boolean isTransitive) {
			this.configurationName = configurationName;
			this.isTransitive = isTransitive;
		}

		public String getConfigurationName() {
			return this.configurationName;
		}

		public boolean isTransitive() {
			return this.isTransitive;
		}
	}
}
