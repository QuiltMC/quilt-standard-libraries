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
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.impl.injection.predicate.cached.ClassInjectionPredicate;

public class DynamicClassInjectionPredicate<P extends ComponentProvider> extends ClassInjectionPredicate implements DynamicInjectionPredicate {
	private final Predicate<P> predicate;

	public DynamicClassInjectionPredicate(Class<P> clazz, Predicate<P> predicate) {
		super(clazz);
		this.predicate = predicate;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean canInject(ComponentProvider provider) {
		// The class will be checked first so the provider will most definitely match the target type.
		return this.predicate.test((P) provider);
	}
}
