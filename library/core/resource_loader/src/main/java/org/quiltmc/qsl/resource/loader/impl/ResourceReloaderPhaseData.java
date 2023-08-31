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

package org.quiltmc.qsl.resource.loader.impl;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.phase.PhaseData;

@ApiStatus.Internal
class ResourceReloaderPhaseData extends PhaseData<ResourceReloader, ResourceReloaderPhaseData> {
	VanillaStatus vanillaStatus = VanillaStatus.NONE;

	ResourceReloaderPhaseData(Identifier id, @Nullable ResourceReloader resourceReloader) {
		super(id, resourceReloader);
	}

	/**
	 * Marks this phase and all preceding phases as running before Vanilla.
	 */
	void markBefore() {
		boolean isAfter = this.vanillaStatus == VanillaStatus.AFTER;

		if (this.vanillaStatus != VanillaStatus.NONE && !isAfter) return;

		this.vanillaStatus = VanillaStatus.BEFORE;

		for (var prev : this.previousPhases) {
			prev.markBefore();
		}
	}

	/**
	 * Marks this phase and all succeeding phases as running after Vanilla.
	 */
	void markAfter() {
		if (this.vanillaStatus != VanillaStatus.NONE) return;

		this.vanillaStatus = VanillaStatus.AFTER;

		for (var next : this.subsequentPhases) {
			next.markAfter();
		}
	}

	void setVanillaStatus(VanillaStatus status) {
		if (this.vanillaStatus == VanillaStatus.NONE) {
			this.vanillaStatus = status;
		}
	}

	@Override
	protected void addSubsequentPhase(ResourceReloaderPhaseData phase) {
		super.addSubsequentPhase(phase);

		if (this.vanillaStatus == VanillaStatus.VANILLA || this.vanillaStatus == VanillaStatus.AFTER) {
			phase.markAfter();
		}
	}

	@Override
	protected void addPreviousPhase(ResourceReloaderPhaseData phase) {
		super.addPreviousPhase(phase);

		if (this.vanillaStatus == VanillaStatus.VANILLA || this.vanillaStatus == VanillaStatus.BEFORE) {
			// We also mark the phase before
			phase.markBefore();
		}
	}

	enum VanillaStatus {
		NONE,
		AFTER,
		BEFORE,
		VANILLA
	}

	static class AfterVanilla extends ResourceReloaderPhaseData {
		AfterVanilla(Identifier id) {
			super(id, null);
			this.setVanillaStatus(VanillaStatus.VANILLA);
		}

		@Override
		public void markBefore() {}

		@Override
		public void markAfter() {}
	}
}
