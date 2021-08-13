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

import org.quiltmc.qsl.block.extensions.api.data.BlockDataKey;
import org.quiltmc.qsl.block.extensions.api.data.ExtraBlockData;
import org.quiltmc.qsl.block.extensions.mixin.AbstractBlockAccessor;
import net.minecraft.block.Block;
import java.util.HashMap;
import java.util.Map;

public final class QuiltBlockInternals {
	private QuiltBlockInternals() { }

	public static ExtraBlockData computeExtraData(Block block) {
		Block.Settings settings = ((AbstractBlockAccessor) block).getSettings();
		BlockSettingsInternals internals = (BlockSettingsInternals) settings;

		ExtraBlockData extraData = internals.qsl$getExtraData();
		if (extraData == null) {
			Map<BlockDataKey<?>, Object> map = internals.qsl$getSettingsMap();
			if (map == null)
				map = new HashMap<>();
			var builder = new ExtraBlockDataImpl.BuilderImpl(map);
			ExtraBlockData.OnBuild.EVENT.invoker().append(block, settings, builder);
			internals.qsl$setExtraData(extraData = builder.build());
		}

		return extraData;
	}
}
