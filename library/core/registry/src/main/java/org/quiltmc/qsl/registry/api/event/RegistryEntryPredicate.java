package org.quiltmc.qsl.registry.api.event;

@FunctionalInterface
public interface RegistryEntryPredicate<V> {
	boolean test(RegistryIterationContext<V> context);

	default RegistryEntryPredicate<V> and(RegistryEntryPredicate<V> other) {
		return context ->
				this.test(context) && other.test(context);
	}
}
