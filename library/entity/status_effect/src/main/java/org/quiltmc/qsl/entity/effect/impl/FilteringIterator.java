/*
 * Copyright 2024 The Quilt Project
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

package org.quiltmc.qsl.entity.effect.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FilteringIterator<T extends @Nullable Object> implements Iterator<T> {
	private static final int STATE_PENDING_FETCH = 0;
	private static final int STATE_READY = 1;
	private static final int STATE_END_OF_DATA = 2;

	private final @NotNull Iterator<T> source;
	private final @NotNull Predicate<? super T> predicate;

	private int state;
	private @Nullable T next;

	public FilteringIterator(@NotNull Iterator<T> source, @NotNull Predicate<? super T> predicate) {
		this.source = source;
		this.predicate = predicate;
	}

	private boolean tryToFetchNext() {
		while (this.source.hasNext()) {
			T element = this.source.next();
			if (this.predicate.test(element)) {
				this.state = STATE_READY;
				this.next = element;
				return true;
			}
		}

		this.state = STATE_END_OF_DATA;
		this.next = null;
		return false;
	}

	@Override
	public boolean hasNext() {
		return switch (this.state) {
			case STATE_PENDING_FETCH -> this.tryToFetchNext();
			case STATE_READY -> true;
			default -> false;
		};
	}

	@Override
	public T next() {
		if (!this.hasNext()) {
			throw new NoSuchElementException();
		}

		T element = this.next;
		this.state = STATE_PENDING_FETCH;
		this.next = null;
		return element;
	}

	@Override
	public void remove() {
		if (!this.hasNext()) {
			throw new NoSuchElementException();
		}

		this.source.remove();
		this.state = STATE_PENDING_FETCH;
		this.next = null;
	}
}
