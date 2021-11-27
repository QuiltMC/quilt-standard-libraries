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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.PriorityQueue;

import com.google.common.annotations.VisibleForTesting;
import org.jetbrains.annotations.ApiStatus;

import org.quiltmc.qsl.base.impl.QuiltBaseImpl;

/**
 * Contains phase-sorting logic for {@link org.quiltmc.qsl.base.api.event.Event}.
 */
@ApiStatus.Internal
public final class PhaseSorting {
	@VisibleForTesting
	public static boolean ENABLE_CYCLE_WARNING = true;

	private PhaseSorting() {
		throw new UnsupportedOperationException("PhaseSorting only contains static-definitions.");
	}

	/**
	 * Deterministically sort a list of phases.
	 * 1) Compute phase SCCs (i.e. cycles).
	 * 2) Sort phases by id within SCCs.
	 * 3) Sort SCCs with respect to each other by respecting constraints, and by id in case of a tie.
	 */
	public static <T> void sortPhases(List<EventPhaseData<T>> sortedPhases) {
		// FIRST KOSARAJU SCC VISIT
		var topoSort = new ArrayList<EventPhaseData<T>>(sortedPhases.size());

		for (var phase : sortedPhases) {
			forwardVisit(phase, null, topoSort);
		}

		clearStatus(topoSort);
		Collections.reverse(topoSort);

		// SECOND KOSARAJU SCC VISIT
		var phaseToScc = new IdentityHashMap<EventPhaseData<T>, PhaseScc<T>>();

		for (var phase : topoSort) {
			if (phase.visitStatus == 0) {
				var sccPhases = new ArrayList<EventPhaseData<T>>();
				// Collect phases in SCC.
				backwardVisit(phase, sccPhases);
				// Sort phases by id.
				sccPhases.sort(Comparator.comparing(p -> p.id));
				// Mark phases as belonging to this SCC.
				var scc = new PhaseScc<>(sccPhases);

				for (var phaseInScc : sccPhases) {
					phaseToScc.put(phaseInScc, scc);
				}
			}
		}

		clearStatus(topoSort);

		// Build SCC graph
		for (var scc : phaseToScc.values()) {
			for (EventPhaseData<T> phase : scc.phases) {
				for (EventPhaseData<T> subsequentPhase : phase.subsequentPhases) {
					PhaseScc<T> subsequentScc = phaseToScc.get(subsequentPhase);

					if (subsequentScc != scc) {
						scc.subsequentSccs.add(subsequentScc);
						subsequentScc.inDegree++;
					}
				}
			}
		}

		// Order SCCs according to priorities. When there is a choice, use the SCC with the lowest id.
		// The priority queue contains all SCCs that currently have 0 in-degree.
		var pq = new PriorityQueue<PhaseScc<T>>(Comparator.comparing(scc -> scc.phases.get(0).id));
		sortedPhases.clear();

		for (var scc : phaseToScc.values()) {
			if (scc.inDegree == 0) {
				pq.add(scc);
				// Prevent adding the same SCC multiple times, as phaseToScc may contain the same value multiple times.
				scc.inDegree = -1;
			}
		}

		while (!pq.isEmpty()) {
			PhaseScc<T> scc = pq.poll();
			sortedPhases.addAll(scc.phases);

			for (var subsequentScc : scc.subsequentSccs) {
				subsequentScc.inDegree--;

				if (subsequentScc.inDegree == 0) {
					pq.add(subsequentScc);
				}
			}
		}
	}

	private static <T> void forwardVisit(EventPhaseData<T> phase, EventPhaseData<T> parent, List<EventPhaseData<T>> toposort) {
		if (phase.visitStatus == 0) {
			// Not yet visited.
			phase.visitStatus = 1;

			for (var data : phase.subsequentPhases) {
				forwardVisit(data, phase, toposort);
			}

			toposort.add(phase);
			phase.visitStatus = 2;
		} else if (phase.visitStatus == 1 && ENABLE_CYCLE_WARNING) {
			// Already visiting, so we have found a cycle.
			QuiltBaseImpl.LOGGER.warn(String.format(
					"Event phase ordering conflict detected.%nEvent phase %s is ordered both before and after event phase %s.",
					phase.id,
					parent.id
			));
		}
	}

	private static <T> void clearStatus(List<EventPhaseData<T>> phases) {
		for (var phase : phases) {
			phase.visitStatus = 0;
		}
	}

	private static <T> void backwardVisit(EventPhaseData<T> phase, List<EventPhaseData<T>> sccPhases) {
		if (phase.visitStatus == 0) {
			phase.visitStatus = 1;
			sccPhases.add(phase);

			for (var data : phase.previousPhases) {
				backwardVisit(data, sccPhases);
			}
		}
	}

	private static class PhaseScc<T> {
		final List<EventPhaseData<T>> phases;
		final List<PhaseScc<T>> subsequentSccs = new ArrayList<>();
		int inDegree = 0;

		private PhaseScc(List<EventPhaseData<T>> phases) {
			this.phases = phases;
		}
	}
}
