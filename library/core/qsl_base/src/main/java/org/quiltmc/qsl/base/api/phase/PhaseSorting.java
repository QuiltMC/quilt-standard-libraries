/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2021 The Quilt Project
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
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.PriorityQueue;

import com.google.common.annotations.VisibleForTesting;

import org.quiltmc.qsl.base.impl.QuiltBaseImpl;

/**
 * Provides the phase-sorting logic of {@link PhaseData}.
 */
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
	 *
	 * @param sortedPhases the phases to sort
	 * @param <T>          the type of data held in a phase
	 * @param <P>          the type of the phase data
	 */
	public static <T, P extends PhaseData<T, P>> void sortPhases(List<P> sortedPhases) {
		// FIRST KOSARAJU SCC VISIT
		var topoSort = new ArrayList<P>(sortedPhases.size());

		for (var phase : sortedPhases) {
			forwardVisit(phase, null, topoSort);
		}

		clearStatus(topoSort);
		Collections.reverse(topoSort);

		// SECOND KOSARAJU SCC VISIT
		var phaseToScc = new IdentityHashMap<P, PhaseScc<T, P>>();

		for (var phase : topoSort) {
			if (phase.visitStatus == PhaseData.VisitStatus.NOT_VISITED) {
				var sccPhases = new ArrayList<P>();
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
			for (P phase : scc.phases) {
				for (P subsequentPhase : phase.subsequentPhases) {
					PhaseScc<T, P> subsequentScc = phaseToScc.get(subsequentPhase);

					if (subsequentScc != scc) {
						scc.subsequentSccs.add(subsequentScc);
						subsequentScc.inDegree++;
					}
				}
			}
		}

		// Order SCCs according to priorities. When there is a choice, use the SCC with the lowest id.
		// The priority queue contains all SCCs that currently have 0 in-degree.
		var pq = new PriorityQueue<PhaseScc<T, P>>(Comparator.comparing(scc -> scc.phases.get(0).id));
		sortedPhases.clear();

		for (var scc : phaseToScc.values()) {
			if (scc.inDegree == 0) {
				pq.add(scc);
				// Prevent adding the same SCC multiple times, as phaseToScc may contain the same value multiple times.
				scc.inDegree = -1;
			}
		}

		while (!pq.isEmpty()) {
			PhaseScc<T, P> scc = pq.poll();
			sortedPhases.addAll(scc.phases);

			for (var subsequentScc : scc.subsequentSccs) {
				subsequentScc.inDegree--;

				if (subsequentScc.inDegree == 0) {
					pq.add(subsequentScc);
				}
			}
		}
	}

	private static <T, P extends PhaseData<T, P>> void forwardVisit(P phase, P parent, List<P> toposort) {
		if (phase.visitStatus == PhaseData.VisitStatus.NOT_VISITED) {
			// Not yet visited.
			phase.visitStatus = PhaseData.VisitStatus.VISITING;

			for (var data : phase.subsequentPhases) {
				forwardVisit(data, phase, toposort);
			}

			toposort.add(phase);
			phase.visitStatus = PhaseData.VisitStatus.VISITED;
		} else if (phase.visitStatus == PhaseData.VisitStatus.VISITING && ENABLE_CYCLE_WARNING) {
			// Already visiting, so we have found a cycle.
			QuiltBaseImpl.LOGGER.warn(String.format(
					"Phase ordering conflict detected.%nPhase %s is ordered both before and after phase %s.",
					phase.id,
					parent.id
			));
		}
	}

	private static <T, P extends PhaseData<T, P>> void clearStatus(List<P> phases) {
		for (var phase : phases) {
			phase.visitStatus = PhaseData.VisitStatus.NOT_VISITED;
		}
	}

	private static <T, P extends PhaseData<T, P>> void backwardVisit(P phase, List<P> sccPhases) {
		if (phase.visitStatus == PhaseData.VisitStatus.NOT_VISITED) {
			phase.visitStatus = PhaseData.VisitStatus.VISITING;
			sccPhases.add(phase);

			for (var data : phase.previousPhases) {
				backwardVisit(data, sccPhases);
			}
		}
	}

	private static class PhaseScc<T, P extends PhaseData<T, P>> {
		final List<P> phases;
		final List<PhaseScc<T, P>> subsequentSccs = new ArrayList<>();
		int inDegree = 0;

		private PhaseScc(List<P> phases) {
			this.phases = phases;
		}
	}
}
