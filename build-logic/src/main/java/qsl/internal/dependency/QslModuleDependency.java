package qsl.internal.dependency;

import javax.inject.Inject;
import org.gradle.api.Named;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public class QslModuleDependency implements Named {
	private final String name;
	private final Property<Boolean> impl;
	private final Property<Boolean> testmod;

	@Inject
	public QslModuleDependency(String name, ObjectFactory objects) {
		this.name = name;

		impl = objects.property(Boolean.class);
		impl.convention(false);

		testmod = objects.property(Boolean.class);
		testmod.convention(false);
	}

	public Property<Boolean> getImpl() {
		return impl;
	}
	public Property<Boolean> getTestmod() {
		return testmod;
	}

	@Override
	public String getName() {
		return name;
	}
}
