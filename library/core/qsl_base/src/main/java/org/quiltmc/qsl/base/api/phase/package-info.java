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

/**
 * <h2>The Phase Sorting APIs.</h2>
 * <p>
 * This module offers a phase sorter, which is a very simple non-cyclic graph solver.
 * This is used for ordering event phases and may be used for other stuff.
 * To sort phases {@linkplain org.quiltmc.qsl.base.api.phase.PhaseSorting#sortPhases(java.util.List) a sort method is provided}.
 *
 * @see org.quiltmc.qsl.base.api.phase.PhaseData
 * @see org.quiltmc.qsl.base.api.phase.PhaseSorting
 */

package org.quiltmc.qsl.base.api.phase;
