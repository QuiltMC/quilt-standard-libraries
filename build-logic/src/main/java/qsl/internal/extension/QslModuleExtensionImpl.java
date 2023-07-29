package qsl.internal.extension;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;

import javax.inject.Inject;

import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import qsl.internal.dependency.QslLibraryDependency;
import qsl.internal.json.Environment;

import org.quiltmc.parsers.json.JsonWriter;

public class QslModuleExtensionImpl extends QslExtension implements QslModuleExtension, Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
	// public properties
	private final Property<String> name;
	private final Property<String> moduleName;
	private final Property<String> id;
	private final Property<String> description;
	private final Property<Environment> environment;
	private final Property<Boolean> hasAccessWidener;
	private final Property<Boolean> hasMixins;
	private final NamedDomainObjectContainer<QslLibraryDependency> moduleDependencyDefinitions;
	private final ListProperty<ProvideEntry> provides;
	private final NamedDomainObjectContainer<NamedWriteOnlyList> entrypoints;
	private final NamedDomainObjectContainer<NamedWriteOnlyList> injectedInterfaces;

	@Inject
	public QslModuleExtensionImpl(ObjectFactory factory, Project project) {
		super(project);
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
		this.provides = factory.listProperty(ProvideEntry.class);
		this.injectedInterfaces = factory.domainObjectContainer(NamedWriteOnlyList.class, n -> new NamedWriteOnlyList(factory, n));
		project.getTasks().findByName("check").dependsOn("checkLicenses");
	}

	@Input
	public Property<String> getModuleName() {
		return this.moduleName;
	}

	@Override
	public String getLibrary() {
		return this.project.getParent().getExtensions().getByType(QslLibraryExtension.class).getLibraryName().get();
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
		return this.hasAccessWidener;
	}

	public void accessWidener() {
		this.hasAccessWidener.set(true);
		this.project.getExtensions()
				.getByType(LoomGradleExtensionAPI.class)
				.getAccessWidenerPath()
				.fileValue(this.project.file("src/main/resources/" + this.id.get() + ".accesswidener"));
	}

	@Input
	public Property<Boolean> getHasMixins() {
		return this.hasMixins;
	}

	public void noMixins() {
		this.hasMixins.set(false);
	}

	@Nested
	public NamedDomainObjectContainer<QslLibraryDependency> getModuleDependencyDefinitions() {
		return this.moduleDependencyDefinitions;
	}

	public void moduleDependencies(Action<NamedDomainObjectContainer<QslLibraryDependency>> configure) {
		configure.execute(this.moduleDependencyDefinitions);
	}

	@Nested
	public ListProperty<ProvideEntry> getProvides() {
		return this.provides;
	}

	@Override
	public void provides(String id, @Nullable String version) {
		this.provides.add(new ProvideEntry(id, version));
	}

	@Nested
	public NamedDomainObjectContainer<NamedWriteOnlyList> getEntrypoints() {
		return this.entrypoints;
	}

	@Override
	public void entrypoints(Action<NamedDomainObjectContainer<NamedWriteOnlyList>> configure) {
		configure.execute(this.entrypoints);
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

	public void setupModuleDependencies() {
		for (QslLibraryDependency library : this.getModuleDependencyDefinitions()) {
			for (QslLibraryDependency.ModuleDependencyInfo info : library.getDependencyInfo().get()) {
				var map = new LinkedHashMap<String, String>(2);
				map.put("path", ":" + library.getName() + ":" + info.module());
				map.put("configuration", "namedElements");

				Dependency dep = this.project.getDependencies().project(map);
				this.project.getDependencies().add(info.type().getConfigurationName(), dep);

				if (info.type().isTransitive()) {
					this.addTransitiveImplementations(library, info);
				}
			}
		}
	}

	/**
	 * This method traverses up the internal QSL module dependency tree, adding the different modules as either runtime or implementation only based on the dependency type for the module.
	 *
	 * @param library The library that the module is in
	 * @param info    The module info to add transitive dependecies from
	 */
	private void addTransitiveImplementations(QslLibraryDependency library, QslLibraryDependency.ModuleDependencyInfo info) {
		Project depProject = this.project.getRootProject().project(":" + library.getName() + ":" + info.module());
		QslModuleExtensionImpl qslModuleExtension = (QslModuleExtensionImpl) depProject.getExtensions().getByType(QslModuleExtension.class);

		for (QslLibraryDependency depLibrary : qslModuleExtension.getModuleDependencyDefinitions()) {
			for (QslLibraryDependency.ModuleDependencyInfo depInfo : depLibrary.getDependencyInfo().get()) {
				if (depInfo.type().isTransitive()) {
					var depMap = new LinkedHashMap<String, String>(2);
					depMap.put("path", ":" + depLibrary.getName() + ":" + depInfo.module());
					depMap.put("configuration", "namedElements");

					Dependency depDep = this.project.getDependencies().project(depMap);
					QslLibraryDependency.ConfigurationType type = switch (depInfo.type()) {
						case API -> QslLibraryDependency.ConfigurationType.IMPLEMENTATION;
						default -> QslLibraryDependency.ConfigurationType.RUNTIME_ONLY;
					};
					this.project.getDependencies().add(type.getConfigurationName(), depDep);

					this.addTransitiveImplementations(depLibrary, depInfo);
				}
			}
		}
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
		public @NotNull String getName() {
			return this.name;
		}

		@Input
		public ListProperty<String> getValues() {
			return this.values;
		}

		public void setValues(Iterable<String> list) {
			this.values.set(list);
		}
	}

	public record ProvideEntry(String id, @Nullable String version) implements Named {
		public void write(JsonWriter writer) throws IOException {
			if (this.version != null) {
				writer.beginObject();
				writer.name("id").value(this.id);
				writer.name("version").value(this.version);
				writer.endObject();
			} else {
				writer.value(this.id);
			}
		}

		@Input
		@Override
		public @NotNull String getName() {
			return this.id;
		}
	}
}
