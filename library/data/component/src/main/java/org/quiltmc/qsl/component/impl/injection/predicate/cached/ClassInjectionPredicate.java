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

import java.util.Arrays;
import java.util.Objects;

import org.quiltmc.qsl.component.api.predicate.InjectionPredicate;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.impl.util.ErrorUtil;

public class ClassInjectionPredicate implements InjectionPredicate {
	protected final Class<?> clazz;

	public ClassInjectionPredicate(Class<?> clazz) {
		if (implementsComponentProvider(clazz)) {
			this.clazz = clazz;
		} else {
			throw ErrorUtil.illegalArgument(
					"Cannot create an injection predicate for a class that doesn't implement ComponentProvider"
			).get();
		}
	}

	private static boolean implementsComponentProvider(Class<?> clazz) {
		return Arrays.stream(clazz.getInterfaces()).anyMatch(it -> it == ComponentProvider.class)
				|| (clazz.getSuperclass() != null && implementsComponentProvider(clazz.getSuperclass()));
	}

	@Override
	public boolean isClassValid(Class<?> clazz) {
		return clazz == this.clazz;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.clazz);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o instanceof ClassInjectionPredicate that) {
			return this.clazz.equals(that.clazz);
		}

		return false;
	}

	@Override
	public String toString() {
		return "ClassInjectionPredicate{clazz=" + this.clazz + '}';
	}
}
