/*
 * Copyright 2021-2023 QuiltMC
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
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.block.Material;
import net.minecraft.block.piston.PistonBehavior;

@Mixin(Material.Builder.class)
public interface MaterialBuilderAccessor {
	@Accessor
	void setPistonBehavior(PistonBehavior behavior);

	@Accessor
	void setBlocksMovement(boolean blocksMovement);

	@Accessor
	void setBurnable(boolean burnable);

	@Accessor
	void setLiquid(boolean liquid);

	@Accessor
	void setReplaceable(boolean replaceable);

	@Accessor
	void setSolid(boolean solid);

	@Accessor
	void setBlocksLight(boolean blocksLight);

	@Invoker
	Material.Builder invokeLightPassesThrough();
}
