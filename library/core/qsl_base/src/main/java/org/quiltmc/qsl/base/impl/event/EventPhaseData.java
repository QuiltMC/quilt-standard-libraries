/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.base.impl.event;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.util.Identifier;

/**
 * Data of an {@link org.quiltmc.qsl.base.api.event.Event} phase.
 */
@ApiStatus.Internal
public final class EventPhaseData<T> {
	final Identifier id;
	T[] listeners;
	final List<EventPhaseData<T>> subsequentPhases = new ArrayList<>();
	final List<EventPhaseData<T>> previousPhases = new ArrayList<>();
	VisitStatus visitStatus = VisitStatus.NOT_VISITED;

	@SuppressWarnings("unchecked")
	public EventPhaseData(Identifier id, Class<?> listenerClass) {
		this.id = id;
		this.listeners = (T[]) Array.newInstance(listenerClass, 0);
	}

	public Identifier id() {
		return this.id;
	}

	public T[] getListeners() {
		return this.listeners;
	}

	public void addListener(T listener) {
		int oldLength = this.listeners.length;
		this.listeners = Arrays.copyOf(listeners, oldLength + 1);
		this.listeners[oldLength] = listener;
	}

	public static <T> void link(EventPhaseData<T> first, EventPhaseData<T> second) {
		first.subsequentPhases.add(second);
		second.previousPhases.add(first);
	}

	enum VisitStatus {
		NOT_VISITED,
		VISITING,
		VISITED
	}
}
