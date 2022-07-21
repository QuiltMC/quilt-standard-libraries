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

public class RedirectedInjectionPredicate extends ClassInjectionPredicate {
	private final Set<Class<?>> redirections;

	public RedirectedInjectionPredicate(Class<?> clazz, Set<Class<?>> redirections) {
		super(clazz);
		this.redirections = Set.copyOf(redirections);
	}

	@Override
	public boolean isClassValid(Class<?> clazz) {
		return super.isClassValid(clazz) || this.redirections.contains(clazz);
	}

	@Override
	public String toString() {
		return "RedirectedInjectionPredicate{redirections=" + this.redirections + ", clazz=" + this.clazz + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RedirectedInjectionPredicate that)) return false;
		if (!super.equals(o)) return false;
		return this.redirections.equals(that.redirections);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), this.redirections);
	}
}
