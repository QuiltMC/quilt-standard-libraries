package org.quiltmc.qsl.component.api.injection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.quiltmc.qsl.base.api.util.Maybe;
import org.quiltmc.qsl.component.api.ComponentFactory;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.injection.predicate.InjectionPredicate;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.component.impl.injection.predicate.cached.ClassInjectionPredicate;
import org.quiltmc.qsl.component.impl.injection.predicate.cached.FilteredInheritedInjectionPredicate;
import org.quiltmc.qsl.component.impl.injection.predicate.cached.InheritedInjectionPredicate;
import org.quiltmc.qsl.component.impl.injection.predicate.dynamic.DynamicWrappedPredicate;
import org.quiltmc.qsl.component.impl.util.ErrorUtil;

public class ComponentInjector<T extends ComponentProvider> {
	protected final Class<T> target;
	protected final List<ComponentEntry<?>> entries;
	protected InjectionPredicate predicate;

	public ComponentInjector(Class<T> target) {
		this.target = target;
		this.predicate = new ClassInjectionPredicate(this.target);
		this.entries = new ArrayList<>();
	}

	public static <T extends ComponentProvider> ComponentInjector<T> injector(Class<?> clazz) {
		Class<T> properClass = ComponentInjector.<T>asProvider(clazz).unwrapOrThrow(
				ErrorUtil.illegalArgument("Class %s is not a ComponentProvider implementor!", clazz)
		);
		return new ComponentInjector<>(properClass);
	}

	public static <T extends ComponentProvider> Maybe<Class<T>> asProvider(Class<?> clazz) {
		var currClass = clazz;

		while (currClass != null) {
			for (Class<?> currInterface : currClass.getInterfaces()) {
				if (currInterface == ComponentProvider.class) {
					// we can safely cast, since the class implements ComponentProvider
					return Maybe.just(clazz).castUnchecked();
				}
			}

			currClass = currClass.getSuperclass();
		}

		return Maybe.nothing();
	}

	public ComponentInjector<T> inherited() {
		this.predicate = new InheritedInjectionPredicate(this.target);
		return this;
	}

	public ComponentInjector<T> exclude(Class<?>... classes) {
		if (this.predicate instanceof InheritedInjectionPredicate) {
			this.predicate = new FilteredInheritedInjectionPredicate(this.target, classes);
			return this;
		}

		throw new IllegalStateException("To exclude classes from an injection, the injection must be inherited!");
	}

	public ComponentInjector<T> dynamic(Predicate<T> dynamicCondition) {
		this.predicate = new DynamicWrappedPredicate<>(this.predicate, dynamicCondition);
		return this;
	}

	public <C> EntryBuilder<C> entry(ComponentType<C> type) {
		return new EntryBuilder<>(type);
	}

	public void inject() {
		ComponentsImpl.inject(this.predicate, this.entries.toArray(ComponentEntry[]::new));
	}

	public class EntryBuilder<E> {
		private final ComponentType<E> type;
		private ComponentFactory<E> factory;

		public EntryBuilder(ComponentType<E> type) {
			this.type = type;
			this.factory = type.defaultFactory();
		}

		public EntryBuilder<E> factory(ComponentFactory<E> factory) {
			this.factory = factory;
			return this;
		}

		public ComponentInjector<T> add() {
			ComponentInjector.this.entries.add(new ComponentEntry<>(this.type, this.factory));
			return ComponentInjector.this;
		}
	}
}
