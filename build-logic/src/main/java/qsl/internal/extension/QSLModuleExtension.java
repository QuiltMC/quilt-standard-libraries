package qsl.internal.extension;

import java.util.LinkedHashMap;
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
import qsl.internal.json.ModJsonObject;

public class QSLModuleExtension {
	private final Project project;
	private final Property<String> moduleName;
	private Action<ModJsonObject> jsonPostProcessor;

	@Inject
	public QSLModuleExtension(ObjectFactory factory, Project project) {
		this.project = project;
		this.moduleName = factory.property(String.class);
		this.moduleName.finalizeValueOnRead();
	}

	@Input
	public Property<String> getModuleName() {
		return this.moduleName;
	}

	public void setModuleName(String name) {
		this.moduleName.set(name);
	}

	public void setVersion(String version) {
		this.project.setVersion(version);
	}

	public void coreDependency(String dependency) {
		this.moduleDependency("core:" + dependency);
	}

	public void interLibraryDependency(String dependency) {
		// TODO:
	}

	public void testingDependency(String dependency) {
		// TODO:
	}

	private void moduleDependency(String dependency) {
		Map<String, String> map = new LinkedHashMap<>(2);
		map.put("path", ":" + dependency);
		map.put("configuration", "dev");

		// Add the module as a dependency
		Dependency project = this.project.getDependencies().project(map);
		this.project.getDependencies().add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, project);

		PublicationContainer publications = this.project.getExtensions().getByType(PublishingExtension.class).getPublications();

		publications.named("mavenJava", MavenPublication.class, publication -> {
			publication.pom(pom -> pom.withXml(xml -> {
				Node dependencies = xml.asNode().appendNode("dependencies");

				dependencies.appendNode("groupId", project.getGroup());
				dependencies.appendNode("artifactId", project.getName());
				dependencies.appendNode("version", project.getVersion());
				dependencies.appendNode("scope", "compile");
			}));
		});
	}

	/**
	 * Registers a post-processor used to add additional data to the quilt.mod.json file for this module.
	 *
	 * @param postProcessor the post processor
	 */
	public void qmj(Action<ModJsonObject> postProcessor) {
		this.jsonPostProcessor = Objects.requireNonNull(postProcessor);
	}
}
