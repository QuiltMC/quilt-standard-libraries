/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2021-2022 QuiltMC
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
		accessor.setPistonBehavior(material.getPistonBehavior());
		accessor.setBlocksMovement(material.blocksMovement());
		accessor.setBurnable(material.isBurnable());
		accessor.setLiquid(material.isLiquid());
		accessor.setReplaceable(material.isReplaceable());
		accessor.setSolid(material.isSolid());
		accessor.setBlocksLight(material.blocksLight());
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
		((MaterialBuilderAccessor) this).invokeLightPassesThrough();
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
		((MaterialBuilderAccessor) this).setPistonBehavior(behavior);
		return this;
	}
}
