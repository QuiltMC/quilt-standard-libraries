package qsl.internal.dependency;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gradle.api.Named;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class QslLibraryDependency implements Named {
	private final String name;
	private final Project project;
	// Reference to the extension's moduleDependencies
	private final List<Dependency> moduleDependencies;

	public QslLibraryDependency(String name, Project project, List<Dependency> moduleDependencies) {
		this.name = name;
		this.project = project;

		this.moduleDependencies = moduleDependencies;
	}

	private void add(String module, ConfigurationType type) {
		Map<String, String> map = new LinkedHashMap<>(2);
		map.put("path", ":" + name + ":" + module);
		map.put("configuration", "dev");

		Dependency dep = this.project.getDependencies().project(map);
		moduleDependencies.add(dep);
		this.project.getDependencies().add(type.getConfigurationName(), dep);
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

	@Override
	public @NotNull String getName() {
		return name;
	}

	/**
	 * The configuration type for the module dependency
	 */
	public enum ConfigurationType {
		API(JavaPlugin.API_CONFIGURATION_NAME),
		IMPLEMENTATION(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME),
		TESTMOD("testmodImplementation"),
		COMPILE_ONLY("compileOnly");

		private final String configurationName;
		ConfigurationType(String configurationName) {
			this.configurationName = configurationName;
		}

		public String getConfigurationName() {
			return configurationName;
		}
	}
}
