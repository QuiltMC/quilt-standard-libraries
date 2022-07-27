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

package org.quiltmc.qsl.component.api.injection;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import org.quiltmc.qsl.component.api.ComponentCreationContext;
import org.quiltmc.qsl.component.api.ComponentFactory;
import org.quiltmc.qsl.component.api.ComponentType;

public class ComponentEntry<C> {
	private final ComponentType<C> type;
	private final ComponentFactory<C> factory;

	public ComponentEntry(ComponentType<C> type, ComponentFactory<C> factory) {
		this.type = type;
		this.factory = factory;
	}

	public ComponentEntry(ComponentType<C> type) {
		this(type, type.defaultFactory());
	}

	public C apply(@Nullable Runnable saveOperation, @Nullable Runnable syncOperation) {
		return this.factory.create(new ComponentCreationContext(saveOperation, syncOperation));
	}

	public ComponentType<C> type() {
		return this.type;
	}

	public ComponentFactory<C> factory() {
		return this.factory;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (ComponentEntry<?>) obj;
		return Objects.equals(this.type, that.type) &&
			   Objects.equals(this.factory, that.factory);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.type, this.factory);
	}

	@Override
	public String toString() {
		return String.format("ComponentEntry[type=%s, factory=%s]", this.type, this.factory);
	}
}
