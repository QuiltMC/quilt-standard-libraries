package qsl.internal.extension;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import qsl.internal.Versions;

public class QslLibraryExtension {
	private final Property<String> libraryName;
	private final Project project;

	@Inject
	public QslLibraryExtension(ObjectFactory factory, Project project) {
		this.libraryName = factory.property(String.class);
		this.libraryName.finalizeValueOnRead();
		this.project = project;
	}

	public Property<String> getLibraryName() {
		return this.libraryName;
	}

	public void setLibraryName(String name) {
		this.libraryName.set(name);
	}

	public void setVersion(String version) {
		this.project.setVersion(version + '+' + Versions.getMinecraftVersionFancyString()
				+ (System.getenv("SNAPSHOTS_URL") != null ? "-SNAPSHOT" : ""));
	}
}
