/*
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

package org.quiltmc.qsl.block.extensions.impl;

import org.quiltmc.qsl.base.api.event.ArrayEvent;
import net.minecraft.block.Block;

public final class QuiltBlockInternals {
	private QuiltBlockInternals() { }

	public static ExtraData computeExtraData(Block.Settings settings) {
		BlockSettingsInternals internals = (BlockSettingsInternals) settings;

		ExtraData extraData = internals.qsl$getExtraData();
		if (extraData == null)
			internals.qsl$setExtraData(extraData = new ExtraData(settings));

		return extraData;
	}

	public interface OnBuild {
		ArrayEvent<OnBuild> EVENT = ArrayEvent.create(OnBuild.class, onBuilds -> (settings, block) -> {
			for (OnBuild callback : onBuilds)
				callback.onBuild(settings, block);
		});

		void onBuild(Block.Settings settings, Block block);
	}

	public static final class ExtraData {
		public ExtraData(Block.Settings settings) { }
	}
}
