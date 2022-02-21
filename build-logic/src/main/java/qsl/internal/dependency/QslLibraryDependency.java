package qsl.internal.dependency;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gradle.api.Named;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
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
		add(module, ConfigurationType.API);
	}

	public void impl(String module) {
		add(module, ConfigurationType.IMPLEMENTATION);
	}

	public void testmodOnly(String module) {
		add(module, ConfigurationType.TESTMOD);
	}

	public void compileOnly(String module) {
		add(module, ConfigurationType.COMPILE_ONLY);
	}

	public ListProperty<ModuleDependencyInfo> getDependencyInfo() {
		return dependencyInfo;
	}

	@Override
	public @NotNull String getName() {
		return name;
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
		API(JavaPlugin.API_CONFIGURATION_NAME),
		IMPLEMENTATION(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME),
		TESTMOD("testmodImplementation"),
		COMPILE_ONLY("compileOnly");

		@Serial
		// increment when changing this class to properly invalidate the generateQmj task
		private static final long serialVersionUID = 1L;
		private final String configurationName;

		ConfigurationType(String configurationName) {
			this.configurationName = configurationName;
		}

		public String getConfigurationName() {
			return configurationName;
		}
	}
}
