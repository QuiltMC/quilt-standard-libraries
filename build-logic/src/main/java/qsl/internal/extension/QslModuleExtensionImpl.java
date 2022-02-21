package qsl.internal.extension;

import java.io.Serial;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

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
import qsl.internal.license.LicenseHeader;
import qsl.internal.task.ApplyLicenseTask;
import qsl.internal.task.CheckLicenseTask;

public class QslModuleExtensionImpl extends QslExtension implements Serializable {
	@Serial
	// increment when changing this class to properly invalidate the generateQmj task
	private static final long serialVersionUID = 2L;
	// public properties
	@Input
	private final Property<String> name;
	@Input
	private final Property<String> library;
	@Input
	private final Property<String> moduleName;
	@Input
	private final Property<String> id;
	@Input
	private final Property<String> description;
	@Input
	private final Property<Environment> environment;
	@Input
	private final Property<Boolean> hasAccessWidener;
	@Input
	private final Property<Boolean> hasMixins;
	@Nested
	private final NamedDomainObjectContainer<QslLibraryDependency> moduleDependencyDefinitions;
	@Nested
	private final NamedDomainObjectContainer<EntrypointObjectHolder> entrypoints;



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
		this.environment = factory.property(Environment.class);
		this.environment.set(Environment.ANY);
		this.environment.finalizeValueOnRead();
		this.hasAccessWidener = factory.property(Boolean.class);
		this.hasAccessWidener.set(false);
		this.hasAccessWidener.finalizeValueOnRead();
		this.hasMixins = factory.property(Boolean.class);
		hasMixins.set(true);
		this.hasMixins.finalizeValueOnRead();
		this.entrypoints = factory.domainObjectContainer(EntrypointObjectHolder.class, n -> new EntrypointObjectHolder(factory, n));
		//this.moduleDependencies = new ArrayList<>();
		LicenseHeader licenseHeader = new LicenseHeader(
				LicenseHeader.Rule.fromFile(project.getRootProject().file("codeformat/FABRIC_MODIFIED_HEADER").toPath()),
				LicenseHeader.Rule.fromFile(project.getRootProject().file("codeformat/HEADER").toPath())
		);

		this.moduleDependencyDefinitions = factory.domainObjectContainer(QslLibraryDependency.class, name -> new QslLibraryDependency(factory, name));

		project.getTasks().register("checkLicenses", CheckLicenseTask.class, licenseHeader);
		project.getTasks().register("applyLicenses", ApplyLicenseTask.class, licenseHeader);
		project.getTasks().findByName("check").dependsOn("checkLicenses");
	}

	public Property<String> getModuleName() {
		return this.moduleName;
	}

	public Property<String> getLibrary() {
		return this.library;
	}

	public Property<String> getName() {
		return this.name;
	}
	public Property<String> getId() {
		return this.id;
	}
	public Property<String> getDescription() {
		return this.description;
	}

	public Property<Boolean> getHasAccessWidener() {
		return hasAccessWidener;
	}

	public void accessWidener() {
		hasAccessWidener.set(true);
		project.getExtensions()
				.getByType(LoomGradleExtensionAPI.class)
				.getAccessWidenerPath()
				.fileValue(project.file("src/main/resources/" + id.get() + ".accessWidener"));
	}
	public Property<Boolean> getHasMixins() {
		return hasMixins;
	}

	public void noMixins() {
		this.hasMixins.set(false);
	}

	public NamedDomainObjectContainer<QslLibraryDependency> getModuleDependencyDefinitions() {
		return moduleDependencyDefinitions;
	}

	public void moduleDependencies(Action<NamedDomainObjectContainer<QslLibraryDependency>> configure) {
		configure.execute(moduleDependencyDefinitions);
	}

	public NamedDomainObjectContainer<EntrypointObjectHolder> getEntrypoints() {
		return entrypoints;
	}

	public void entrypoints(Action<NamedDomainObjectContainer<EntrypointObjectHolder>> configure) {
		configure.execute(entrypoints);
	}


	public void clientOnly() {
		this.environment.set(Environment.CLIENT_ONLY);
	}

	public void dedicatedServerOnly() {
		this.environment.set(Environment.DEDICATED_SERVER_ONLY);
	}

	public Property<Environment> getEnvironment() {
		return this.environment;
	}

	public static class EntrypointObjectHolder implements Named {
		private final String name;
		private final ListProperty<String> values;

		public EntrypointObjectHolder(ObjectFactory factory, String name) {
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
