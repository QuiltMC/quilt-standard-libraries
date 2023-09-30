/*
 * Copyright 2022 The Quilt Project
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
import java.util.Arrays;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModInternal;
import org.quiltmc.qsl.base.api.phase.PhaseData;

@ModInternal
@ApiStatus.Internal
public final class EventPhaseData<T> extends PhaseData<T[], EventPhaseData<T>> {
	@SuppressWarnings("unchecked")
	public EventPhaseData(Identifier id, Class<?> listenerClass) {
		super(id, (T[]) Array.newInstance(listenerClass, 0));
	}

	public void addListener(T listener) {
		int oldLength = this.data.length;
		this.data = Arrays.copyOf(data, oldLength + 1);
		this.data[oldLength] = listener;
	}
}
