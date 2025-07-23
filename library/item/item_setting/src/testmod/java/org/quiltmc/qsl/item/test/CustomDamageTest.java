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

package org.quiltmc.qsl.item.test;

import static org.quiltmc.qsl.item.test.QuiltItemSettingsExtensionsTests.createId;
import static org.quiltmc.qsl.item.test.QuiltItemSettingsExtensionsTests.registerItem;

import net.minecraft.component.DataComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.unmapped.C_bemqmqey;
import net.minecraft.util.dynamic.Codecs;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.item.setting.api.CustomDamageHandler;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettingsExtensions;

public class CustomDamageTest implements ModInitializer {
	public static final DataComponentType<Integer> WEIRD = Registry.register(
			Registries.DATA_COMPONENT_TYPE,
			createId("weird"),
			DataComponentType.<Integer>builder()
				.codec(Codecs.NONNEGATIVE_INT)
				.packetCodec(PacketCodecs.VAR_INT)
				.build()
	);

	@Override
	public void onInitialize(ModContainer mod) {
		Item.Settings weirdPickSettings = new Item.Settings();
		weirdPickSettings
			// method_66330 is pickaxe
			// C_bemqmqey.INCORRECT_FOR_GOLD_TOOL is ToolMaterial.GOLD
			.method_66330(C_bemqmqey.INCORRECT_FOR_GOLD_TOOL, 1.0F, -2.8F);

		registerItem("weird_pickaxe", WeirdPick::new, weirdPickSettings);
	}

	public static final CustomDamageHandler WEIRD_DAMAGE_HANDLER = (stack, amount, entity, slot, breakCallback) -> {
		// If sneaking, apply all damage to vanilla. Otherwise,
		// increment a tag on the stack by one and don't apply any damage.
		if (entity.isSneaking()) {
			return amount;
		} else {
			// Need the max because the value could wrap around Integer.MAX_VALUE
			stack.set(WEIRD, Math.max(0, stack.getOrDefault(WEIRD, 0) + 1));
			return 0;
		}
	};

	public static class WeirdPick extends Item {
		protected WeirdPick(Item.Settings settings) {
			super(((QuiltItemSettingsExtensions) settings).customDamage(WEIRD_DAMAGE_HANDLER));
		}

		@Override
		public Text getName(ItemStack stack) {
			int weirdValue = stack.getOrDefault(WEIRD, 0);
			return super.getName(stack).copy().append(" (Weird Value: " + weirdValue + ")");
		}
	}
}
