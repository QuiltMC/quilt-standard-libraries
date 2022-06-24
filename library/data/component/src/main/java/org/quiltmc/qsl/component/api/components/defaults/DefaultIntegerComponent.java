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

package org.quiltmc.qsl.component.api.components.defaults;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.components.IntegerComponent;

public class DefaultIntegerComponent implements IntegerComponent {

	private int value;
	@Nullable
	private Runnable saveOperation;

	public DefaultIntegerComponent(int defaultValue) {
		this.value = defaultValue;
	}

	public DefaultIntegerComponent() {
		this(0);
	}

	@Override
	public int get() {
		return this.value;
	}

	@Override
	public void set(int value) {
		this.value = value;
		this.saveNeeded();
	}

	@Override
	public void increment() {
		this.value++;
		this.saveNeeded();
	}

	@Override
	public void decrement() {
		this.value--;
		this.saveNeeded();
	}

	@Override
	public void saveNeeded() {
		if (this.saveOperation != null) {
			this.saveOperation.run();
		}
	}

	@Override
	public void setSaveOperation(@Nullable Runnable runnable) {
		this.saveOperation = runnable;
	}
}
