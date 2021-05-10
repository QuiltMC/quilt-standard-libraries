package qsl.internal;

import java.util.Objects;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
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
		// TODO:
	}

	public void interLibraryDependency(String dependency) {
		// TODO:
	}

	public void testingDependency(String dependency) {
		// TODO:
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
