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

package org.quiltmc.qsl.component.impl.components;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.components.FloatComponent;

public class DefaultFloatComponent implements FloatComponent {
	private float value;
	@Nullable
	private Runnable saveOperation;

	public DefaultFloatComponent() {
		this(0);
	}

	public DefaultFloatComponent(float initialValue) {
		this.value = initialValue;
	}

	@Override
	public float get() {
		return this.value;
	}

	@Override
	public void set(float value) {
		this.value = value;
		this.saveNeeded();
	}

	@Override
	public void setSaveOperation(@Nullable Runnable runnable) {
		this.saveOperation = runnable;
	}

	@Override
	public @Nullable Runnable getSaveOperation() {
		return this.saveOperation;
	}
}
