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

package org.quiltmc.qsl.block.extensions.api;

import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.piston.PistonBehavior;
import org.quiltmc.qsl.block.extensions.mixin.MaterialBuilderAccessor;

/**
 * An extended variant of the {@link Material.Builder} class, which provides extra methods for customization.
 */
public class QuiltMaterialBuilder extends Material.Builder {
	public QuiltMaterialBuilder(MapColor color) {
		super(color);
	}

	public static QuiltMaterialBuilder copyOf(Material material, MapColor color) {
		var builder = new QuiltMaterialBuilder(color);
		@SuppressWarnings("ConstantConditions") var accessor = (MaterialBuilderAccessor) builder;
		accessor.qsl$setPistonBehavior(material.getPistonBehavior());
		accessor.qsl$setBlocksMovement(material.blocksMovement());
		accessor.qsl$setBurnable(material.isBurnable());
		accessor.qsl$setLiquid(material.isLiquid());
		accessor.qsl$setReplaceable(material.isReplaceable());
		accessor.qsl$setSolid(material.isSolid());
		accessor.qsl$setBlocksLight(material.blocksLight());
		return builder;
	}

	public static QuiltMaterialBuilder copyOf(Material material) {
		return copyOf(material, material.getColor());
	}

	@Override
	public QuiltMaterialBuilder liquid() {
		super.liquid();
		return this;
	}

	@Override
	public QuiltMaterialBuilder notSolid() {
		super.notSolid();
		return this;
	}

	@Override
	public QuiltMaterialBuilder allowsMovement() {
		super.allowsMovement();
		return this;
	}

	/**
	 * Makes light pass through blocks of this material.
	 *
	 * @return this builder
	 */
	public QuiltMaterialBuilder lightPassesThrough() {
		((MaterialBuilderAccessor) this).qsl$callLightPassesThrough();
		return this;
	}

	@Override
	public QuiltMaterialBuilder burnable() {
		super.burnable();
		return this;
	}

	@Override
	public QuiltMaterialBuilder replaceable() {
		super.replaceable();
		return this;
	}

	@Override
	public QuiltMaterialBuilder destroyedByPiston() {
		super.destroyedByPiston();
		return this;
	}

	@Override
	public QuiltMaterialBuilder blocksPistons() {
		super.blocksPistons();
		return this;
	}

	/**
	 * Sets the behavior of pistons when interacting with blocks of this material.
	 *
	 * @param behavior new piston behavior
	 * @return this builder
	 */
	public QuiltMaterialBuilder pistonBehavior(PistonBehavior behavior) {
		((MaterialBuilderAccessor) this).qsl$setPistonBehavior(behavior);
		return this;
	}
}
