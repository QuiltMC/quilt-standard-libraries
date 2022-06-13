package org.quiltmc.qsl.component.api;

public interface ComponentInjectionPredicate {

	boolean canInject(ComponentProvider provider);

}
