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

package org.quiltmc.qsl.component.api.predicates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FilteredInheritedInjectionPredicate extends InheritedInjectionPredicate {
	private final List<Class<?>> exceptions;

	public FilteredInheritedInjectionPredicate(Class<?> clazz, Class<?>[] exceptions) {
		super(clazz);
		this.exceptions = new ArrayList<>(Arrays.asList(exceptions));
	}

	@Override
	public boolean canInject(Class<?> current) {
		return !this.exceptions.contains(current) && super.canInject(current);
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
}
