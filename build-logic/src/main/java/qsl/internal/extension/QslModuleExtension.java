package qsl.internal.extension;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import groovy.util.Node;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.Input;
import qsl.internal.GroovyXml;
import qsl.internal.Versions;
import qsl.internal.json.ModJsonObject;
import qsl.internal.license.LicenseHeader;
import qsl.internal.task.ApplyLicenseTask;
import qsl.internal.task.CheckLicenseTask;

public class QslModuleExtension {
	private final Project project;
	private final Property<String> library;
	private final Property<String> moduleName;
	private final List<Dependency> moduleDependencies;
	private final LicenseHeader licenseHeader;
	private Action<ModJsonObject> jsonPostProcessor;

	@Inject
	public QslModuleExtension(ObjectFactory factory, Project project) {
		this.project = project;
		this.library = factory.property(String.class);
		this.library.finalizeValueOnRead();
		this.moduleName = factory.property(String.class);
		this.moduleName.finalizeValueOnRead();
		this.moduleDependencies = new ArrayList<>();
		this.licenseHeader = new LicenseHeader(
				LicenseHeader.Rule.fromFile(project.getRootProject().file("codeformat/FABRIC_MODIFIED_HEADER").toPath()),
				LicenseHeader.Rule.fromFile(project.getRootProject().file("codeformat/HEADER").toPath())
		);

		project.getTasks().register("checkLicenses", CheckLicenseTask.class, this.licenseHeader);
		project.getTasks().register("applyLicenses", ApplyLicenseTask.class, this.licenseHeader);
		project.getTasks().findByName("check").dependsOn("checkLicenses");
	}

	@Input
	public Property<String> getModuleName() {
		return this.moduleName;
	}

	public void setModuleName(String name) {
		this.moduleName.set(name);
	}

	@Input
	public Property<String> getLibrary() {
		return this.library;
	}

	public void setLibrary(String name) {
		this.library.set(name);
	}

	public void setVersion(String version) {
		this.project.setVersion(version + '+' + Versions.getMinecraftVersionFancyString()
				+ (System.getenv("SNAPSHOTS_URL") != null ? "-SNAPSHOT" : ""));
	}

	private Dependency getCoreModule(String module) {
		Map<String, String> map = new LinkedHashMap<>(2);
		map.put("path", ":core:" + module);
		map.put("configuration", "dev");

		return this.project.getDependencies().project(map);
	}

	public void coreDependencies(Iterable<String> dependencies) {
		for (String dependency : dependencies) {
			Dependency project = this.getCoreModule(dependency);
			this.moduleDependencies.add(project);
			this.project.getDependencies().add(JavaPlugin.API_CONFIGURATION_NAME, project);
		}
	}

	public void coreTestmodDependencies(Iterable<String> dependencies) {
		for (String dependency : dependencies) {
			Dependency project = this.getCoreModule(dependency);
			this.moduleDependencies.add(project);
			this.project.getDependencies().add("testmodImplementation", project);
		}
	}

	public void interLibraryDependencies(Iterable<String> dependencies) {
		String library = this.getLibrary().get();

		for (String dependency : dependencies) {
			Map<String, String> map = new LinkedHashMap<>(2);
			map.put("path", ":" + library + ":" + dependency);
			map.put("configuration", "dev");

			Dependency project = this.project.getDependencies().project(map);
			this.moduleDependencies.add(project);
			this.project.getDependencies().add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, project);
		}
	}

	public void setupModuleDependencies() {
		PublicationContainer publications = this.project.getExtensions().getByType(PublishingExtension.class).getPublications();

		publications.named("mavenJava", MavenPublication.class, publication -> {
			publication.pom(pom -> pom.withXml(xml -> {
				Node pomDeps = GroovyXml.getOrCreateNode(xml.asNode(), "dependencies");

				for (Dependency dependency : this.moduleDependencies) {
					Node pomDep = pomDeps.appendNode("dependency");
					pomDep.appendNode("groupId", dependency.getGroup());
					pomDep.appendNode("artifactId", dependency.getName());
					pomDep.appendNode("version", dependency.getVersion());
					pomDep.appendNode("scope", "compile");
				}
			}));
		});
	}

	/**
	 * Registers a post-processor used to add additional data to the quilt.mod.json file for this module.
	 *
	 * @param postProcessor the post processor
	 */
	public void modJson(Action<ModJsonObject> postProcessor) {
		this.jsonPostProcessor = Objects.requireNonNull(postProcessor);
	}
}
