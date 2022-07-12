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
import java.util.Set;

public class FilteredInheritedInjectionPredicate extends InheritedInjectionPredicate {
	private final Set<Class<?>> exceptions;

	public FilteredInheritedInjectionPredicate(Class<?> clazz, Class<?>... exceptions) {
		super(clazz);
		this.exceptions = Set.of(exceptions);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + 37 * Objects.hash(this.exceptions);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof FilteredInheritedInjectionPredicate that)) return false;
		if (!super.equals(o)) return false;
		return exceptions.equals(that.exceptions);
	}

	@Override
	public String toString() {
		return "FilteredInheritedInjectionPredicate{clazz=" + clazz + ", exceptions=" + exceptions + '}';
	}

	@Override
	public boolean isClassValid(Class<?> clazz) {
		return super.isClassValid(clazz) && !this.exceptions.contains(clazz);
	}
}
