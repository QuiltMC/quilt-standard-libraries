package qsl.internal.extension;

import groovy.util.Node;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import qsl.internal.GroovyXml;
import qsl.internal.dependency.QslLibraryDependency;
import qsl.internal.json.Environment;

import javax.inject.Inject;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QslModuleExtensionImpl extends QslExtension implements QslModuleExtension, Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
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

	public void setupModuleDependencies() {
		List<Dependency> deps = new ArrayList<>();

		for (QslLibraryDependency library : this.getModuleDependencyDefinitions()) {
			for (QslLibraryDependency.ModuleDependencyInfo info : library.getDependencyInfo().get()) {
				Map<String, String> map = new LinkedHashMap<>(2);
				map.put("path", ":" + library.getName() + ":" + info.module());
				map.put("configuration", "dev");

				Dependency dep = project.getDependencies().project(map);
				deps.add(dep);
				project.getDependencies().add(info.type().getConfigurationName(), dep);

				if (info.type().isTransitive()) {
					addTransitiveImplementations(library, info);
				}
			}
		}

		PublicationContainer publications = this.project.getExtensions().getByType(PublishingExtension.class).getPublications();

		publications.named("mavenJava", MavenPublication.class, publication -> {
			publication.pom(pom -> pom.withXml(xml -> {
				Node pomDeps = GroovyXml.getOrCreateNode(xml.asNode(), "dependencies");

				for (Dependency dependency : deps) {
					Node pomDep = pomDeps.appendNode("dependency");
					pomDep.appendNode("groupId", dependency.getGroup());
					pomDep.appendNode("artifactId", dependency.getName());
					pomDep.appendNode("version", dependency.getVersion());
					pomDep.appendNode("scope", "compile");
				}
			}));
		});
	}

	private void addTransitiveImplementations(QslLibraryDependency library, QslLibraryDependency.ModuleDependencyInfo info) {
		Project depProject = project.getRootProject().project(":" + library.getName() + ":" + info.module());
		QslModuleExtensionImpl qslModuleExtension = (QslModuleExtensionImpl) depProject.getExtensions().getByType(QslModuleExtension.class);
		for (QslLibraryDependency depLibrary : qslModuleExtension.getModuleDependencyDefinitions()) {
			for (QslLibraryDependency.ModuleDependencyInfo depInfo : depLibrary.getDependencyInfo().get()) {
				if (depInfo.type().isTransitive()) {
					Map<String, String> depMap = new LinkedHashMap<>(2);
					depMap.put("path", ":" + depLibrary.getName() + ":" + depInfo.module());
					depMap.put("configuration", "namedElements");

					Dependency depDep = project.getDependencies().project(depMap);
					QslLibraryDependency.ConfigurationType type = switch (depInfo.type()) {
						case API -> QslLibraryDependency.ConfigurationType.IMPLEMENTATION;
						default -> QslLibraryDependency.ConfigurationType.RUNTIME_ONLY;
					};
					project.getDependencies().add(type.getConfigurationName(), depDep);

					addTransitiveImplementations(depLibrary, depInfo);
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
