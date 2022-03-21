package qsl.internal.extension;

import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import qsl.internal.dependency.QslLibraryDependency;
import qsl.internal.json.Environment;

import javax.inject.Inject;
import java.io.Serial;
import java.io.Serializable;

public class QslModuleExtensionImpl extends QslExtension implements QslModuleExtension, Serializable {
	@Serial
	// increment when changing this class to properly invalidate the generateQmj task
	private static final long serialVersionUID = 3L;
	// public properties
	private final Property<String> name;
	private final Property<String> library;
	private final Property<String> moduleName;
	private final Property<String> id;
	private final Property<String> description;
	private final Property<Environment> environment;
	private final Property<Boolean> hasAccessWidener;
	private final Property<Boolean> hasMixins;
	private final NamedDomainObjectContainer<QslLibraryDependency> moduleDependencyDefinitions;
	private final NamedDomainObjectContainer<NamedWriteOnlyList> entrypoints;
	private final NamedDomainObjectContainer<NamedWriteOnlyList> injectedInterfaces;

	@Inject
	public QslModuleExtensionImpl(ObjectFactory factory, Project project) {
		super(project);
		this.library = factory.property(String.class);
		this.library.finalizeValueOnRead();
		this.moduleName = factory.property(String.class);
		this.moduleName.finalizeValueOnRead();
		this.id = factory.property(String.class);
		this.id.finalizeValueOnRead();
		this.name = factory.property(String.class);
		this.name.finalizeValueOnRead();
		this.description = factory.property(String.class);
		this.description.finalizeValueOnRead();
		this.environment = factory.property(Environment.class).convention(Environment.ANY);
		this.environment.finalizeValueOnRead();
		this.hasAccessWidener = factory.property(Boolean.class).convention(false);
		this.hasAccessWidener.finalizeValueOnRead();
		this.hasMixins = factory.property(Boolean.class).convention(true);
		this.hasMixins.finalizeValueOnRead();
		this.entrypoints = factory.domainObjectContainer(NamedWriteOnlyList.class, n -> new NamedWriteOnlyList(factory, n));
		this.moduleDependencyDefinitions = factory.domainObjectContainer(QslLibraryDependency.class, name -> new QslLibraryDependency(factory, name));
		this.injectedInterfaces = factory.domainObjectContainer(NamedWriteOnlyList.class, n -> new NamedWriteOnlyList(factory, n));
		project.getTasks().findByName("check").dependsOn("checkLicenses");
	}

	@Input
	public Property<String> getModuleName() {
		return this.moduleName;
	}

	@Input
	public Property<String> getLibrary() {
		return this.library;
	}

	@Input
	public Property<String> getName() {
		return this.name;
	}
	@Input
	public Property<String> getId() {
		return this.id;
	}
	@Input
	public Property<String> getDescription() {
		return this.description;
	}

	@Input
	public Property<Boolean> getHasAccessWidener() {
		return hasAccessWidener;
	}

	public void accessWidener() {
		hasAccessWidener.set(true);
		project.getExtensions()
				.getByType(LoomGradleExtensionAPI.class)
				.getAccessWidenerPath()
				.fileValue(project.file("src/main/resources/" + id.get() + ".accesswidener"));
	}

	@Input
	public Property<Boolean> getHasMixins() {
		return hasMixins;
	}

	public void noMixins() {
		this.hasMixins.set(false);
	}

	@Nested
	public NamedDomainObjectContainer<QslLibraryDependency> getModuleDependencyDefinitions() {
		return moduleDependencyDefinitions;
	}

	public void moduleDependencies(Action<NamedDomainObjectContainer<QslLibraryDependency>> configure) {
		configure.execute(moduleDependencyDefinitions);
	}

	@Nested
	public NamedDomainObjectContainer<NamedWriteOnlyList> getEntrypoints() {
		return entrypoints;
	}

	public void entrypoints(Action<NamedDomainObjectContainer<NamedWriteOnlyList>> configure) {
		configure.execute(entrypoints);
	}

	@Override
	public void injectedInterface(String minecraftClass, Action<NamedWriteOnlyList> action) {
		action.execute(this.injectedInterfaces.create(minecraftClass));
	}

	@Nested
	public NamedDomainObjectContainer<NamedWriteOnlyList> getInjectedInterfaces() {
		return this.injectedInterfaces;
	}

	public void clientOnly() {
		this.environment.set(Environment.CLIENT_ONLY);
	}

	public void dedicatedServerOnly() {
		this.environment.set(Environment.DEDICATED_SERVER_ONLY);
	}

	@Input
	public Property<Environment> getEnvironment() {
		return this.environment;
	}

	public static class NamedWriteOnlyList implements Named {
		private final String name;
		private final ListProperty<String> values;

		public NamedWriteOnlyList(ObjectFactory factory, String name) {
			this.values = factory.listProperty(String.class);
			this.name = name;
		}

		@Override
		@Input
		public String getName() {
			return name;
		}

		@Input
		public ListProperty<String> getValues() {
			return values;
		}

		public void setValues(Iterable<String> list) {
			this.values.set(list);
		}
	}
}
