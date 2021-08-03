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

package org.quiltmc.qsl.block.extensions.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import org.quiltmc.qsl.block.extensions.api.data.ExtraBlockData;
import org.quiltmc.qsl.block.extensions.impl.BlockSettingsInternals;
import net.minecraft.block.AbstractBlock;

@Mixin(AbstractBlock.Settings.class)
public abstract class AbstractBlockSettingsMixin implements BlockSettingsInternals {
	@Unique private ExtraBlockData qsl$extraData;

	@Override
	public ExtraBlockData qsl$getExtraData() {
		return this.qsl$extraData;
	}

	@Override
	public void qsl$setExtraData(ExtraBlockData extraData) {
		this.qsl$extraData = extraData;
	}
}
