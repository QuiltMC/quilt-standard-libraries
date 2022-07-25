/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2021-2022 QuiltMC
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

package org.quiltmc.qsl.base.api.phase;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.util.Identifier;

/**
 * Data of a phase.
 *
 * @param <T> the type of data held in a phase
 * @param <P> the type of the phase data
 */
public class PhaseData<T, P extends PhaseData<T, P>> {
	final Identifier id;
	protected T data;
	protected final List<P> subsequentPhases = new ArrayList<>();
	protected final List<P> previousPhases = new ArrayList<>();
	VisitStatus visitStatus = VisitStatus.NOT_VISITED;

	public PhaseData(Identifier id, T data) {
		this.id = id;
		this.data = data;
	}

	/**
	 * {@return the identifier of this phase}
	 */
	public Identifier getId() {
		return this.id;
	}

	/**
	 * {@return the data held by this phase}
	 */
	public T getData() {
		return this.data;
	}

	protected void addSubsequentPhase(P phase) {
		this.subsequentPhases.add(phase);
	}

	protected void addPreviousPhase(P phase) {
		this.previousPhases.add(phase);
	}

	/**
	 * Links two given phases together.
	 *
	 * @param first  the phase that should be ordered first
	 * @param second the phase that should be ordered second
	 * @param <T>    the type of data held by the phases
	 */
	public static <T, P extends PhaseData<T, P>> void link(P first, P second) {
		first.addSubsequentPhase(second);
		second.addPreviousPhase(first);
	}

	enum VisitStatus {
		NOT_VISITED,
		VISITING,
		VISITED
	}
}
