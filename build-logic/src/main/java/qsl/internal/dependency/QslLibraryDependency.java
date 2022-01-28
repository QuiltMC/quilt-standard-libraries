package qsl.internal.dependency;

import org.gradle.api.Named;
import org.gradle.api.Project;
import org.gradle.api.internal.AbstractNamedDomainObjectContainer;
import org.gradle.api.internal.CollectionCallbackActionDecorator;
import org.gradle.api.model.ObjectFactory;
import org.gradle.internal.reflect.DirectInstantiator;
import org.jetbrains.annotations.NotNull;

public class QslLibraryDependency extends AbstractNamedDomainObjectContainer<QslModuleDependency> implements Named {
	private final String name;
	private final ObjectFactory objects;

	public QslLibraryDependency(String name, Project project) {
		super(QslModuleDependency.class, DirectInstantiator.INSTANCE, Namer.INSTANCE, CollectionCallbackActionDecorator.NOOP);
		this.name = name;
		this.objects = project.getObjects();
	}

	@Override
	public @NotNull String getName() {
		return name;
	}

	@Override
	protected QslModuleDependency doCreate(String s) {
		return objects.newInstance(QslModuleDependency.class, s, objects);
	}
}
