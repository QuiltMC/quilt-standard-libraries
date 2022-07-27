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

package org.quiltmc.qsl.component.impl.injection.predicate.cached;

import java.util.Objects;

public class InheritedInjectionPredicate extends ClassInjectionPredicate {
	public InheritedInjectionPredicate(Class<?> clazz) {
		super(clazz);
	}

	@Override
	public boolean isClassValid(Class<?> clazz) {
		return this.clazz == clazz || (clazz != null && this.isClassValid(clazz.getSuperclass()));
	}

	@Override
	public int hashCode() {
		return super.hashCode() + 67 * Objects.hash(this.getClass());
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof InheritedInjectionPredicate && super.equals(o);
	}

	@Override
	public String toString() {
		return "InheritedInjectionPredicate{clazz=" + this.clazz + '}';
	}
}
