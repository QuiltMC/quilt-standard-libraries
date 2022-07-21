/*
 * Copyright 2022 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.component.impl.injection.predicate.dynamic;

import java.util.function.Predicate;

import org.quiltmc.qsl.component.api.predicate.DynamicInjectionPredicate;
import org.quiltmc.qsl.component.api.predicate.InjectionPredicate;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;

public class DynamicWrappedPredicate<P extends ComponentProvider> implements DynamicInjectionPredicate {
	private final InjectionPredicate wrapped;
	private final Predicate<P> predicate;

	public DynamicWrappedPredicate(InjectionPredicate wrapped, Predicate<P> predicate) {
		this.wrapped = wrapped;
		this.predicate = predicate;
	}

	@Override
	public boolean isClassValid(Class<?> clazz) {
		return this.wrapped.isClassValid(clazz);
	}

	@SuppressWarnings("unchecked") // Whoever hands us the provider should make sure it's valid!
	@Override
	public boolean canInject(ComponentProvider provider) {
		return this.predicate.test((P) provider);
	}
}
