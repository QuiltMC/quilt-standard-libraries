package qsl.internal.extension;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public class QslLibraryExtension extends QslExtension {
	private final Property<String> libraryName;

	@Inject
	public QslLibraryExtension(ObjectFactory factory, Project project) {
		super(project);
		this.libraryName = factory.property(String.class);
		this.libraryName.finalizeValueOnRead();
	}

	public Property<String> getLibraryName() {
		return this.libraryName;
	}

	public void setLibraryName(String name) {
		this.libraryName.set(name);
	}
}
