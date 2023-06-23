/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.tooltip.test.client;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.tooltip.api.client.ItemTooltipCallback;
import org.quiltmc.qsl.tooltip.api.client.TooltipComponentCallback;
import org.quiltmc.qsl.tooltip.test.TooltipTestMod;

@ClientOnly
public final class ClientTooltipTestMod implements ItemTooltipCallback, TooltipComponentCallback {
	@Override
	public @Nullable TooltipComponent getComponent(TooltipData data) {
		if (data instanceof TooltipTestMod.Data customData) {
			return TooltipComponent.of(Text.literal(customData.message()).formatted(Formatting.GREEN).asOrderedText());
		}

		return null;
	}

	@Override
	public void onTooltipRequest(ItemStack stack, @Nullable PlayerEntity player, TooltipContext context, List<Text> lines) {
		lines.add(Text.literal("Fancy tooltips").formatted(Formatting.LIGHT_PURPLE));
	}
}
