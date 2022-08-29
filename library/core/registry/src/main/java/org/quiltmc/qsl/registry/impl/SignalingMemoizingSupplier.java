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

package org.quiltmc.qsl.registry.impl;

import com.google.common.base.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * A memoizing supplier that exposes its "initialized" flag.
 * @param <T> the returned value's type
 * @see com.google.common.base.Suppliers#memoize(Supplier)
 */
@SuppressWarnings("Guava") // intentional
public final class SignalingMemoizingSupplier<T> implements Supplier<T> {
	private final Supplier<T> delegate;
	private volatile boolean initialized;
	private T value;

	public SignalingMemoizingSupplier(@NotNull Supplier<T> delegate) {
		this.delegate = delegate;
		this.initialized = false;
	}

	@Override
	public T get() {
		if (!this.initialized) {
			synchronized (this) {
				if (!this.initialized) {
					this.value = this.delegate.get();
					this.initialized = true;
				}
			}
		}

		return this.value;
	}

	public boolean isInitialized() {
		return this.initialized;
	}
}
