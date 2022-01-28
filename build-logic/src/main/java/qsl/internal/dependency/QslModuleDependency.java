package qsl.internal.dependency;

import javax.inject.Inject;
import org.gradle.api.Named;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.NotNull;

public class QslModuleDependency implements Named {
	private final String name;
	private final Property<ConfigurationType> configuration;

	@Inject
	public QslModuleDependency(String name, ObjectFactory objects) {
		this.name = name;

		configuration = objects.property(ConfigurationType.class);
		configuration.convention(ConfigurationType.API);
	}

	public Property<ConfigurationType> getConfiguration() {
		return configuration;
	}

	public void testmod() {
		configuration.set(ConfigurationType.TESTMOD);
	}

	public void api() {
		configuration.set(ConfigurationType.API);
	}

	public void impl() {
		configuration.set(ConfigurationType.IMPLEMENTATION);
	}

	@Override
	public @NotNull String getName() {
		return name;
	}

	/**
	 * The configuration type for the module dependency
	 */
	public enum ConfigurationType {
		API(JavaPlugin.API_CONFIGURATION_NAME), IMPLEMENTATION(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME), TESTMOD("testmodImplementation");

		private final String configurationName;
		ConfigurationType(String configurationName) {
			this.configurationName = configurationName;
		}

		public String getConfigurationName() {
			return configurationName;
		}
	}
}
