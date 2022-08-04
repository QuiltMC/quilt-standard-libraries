/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.fluid.fluid_extensions;

import net.minecraft.block.Blocks;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.fluid.impl.QuiltFluid;
import org.quiltmc.qsl.fluid.api.QuiltFluidBlock;

public class QuiltFluidTest implements ModInitializer {
	public static QuiltFluid STILL_OIL;
	public static QuiltFluid FLOWING_OIL;
	public static Item OIL_BUCKET;
	public static QuiltFluidBlock OIL;

	@Override
	public void onInitialize(ModContainer mod) {
		STILL_OIL = Registry.register(Registry.FLUID, new Identifier("quilt_fluid_api", "oil"), new OilFluid.Still());
		FLOWING_OIL = Registry.register(Registry.FLUID, new Identifier("quilt_fluid_api", "flowing_oil"), new OilFluid.Flowing());
		OIL_BUCKET = Registry.register(Registry.ITEM, new Identifier("quilt_fluid_api", "oil_bucket"),
				new BucketItem(STILL_OIL, new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1)));

		OIL = Registry.register(Registry.BLOCK, new Identifier("quilt_fluid_api", "oil"), new QuiltFluidBlock(STILL_OIL, QuiltBlockSettings.copy(Blocks.WATER)));
	}
}
