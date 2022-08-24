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

package org.quiltmc.qsl.fluid.api;

/**
 * @apiNote - Helper class for organizing the horizontalViscosity and the speed, without having to deal with magic array indexes.
 * @param horizontalViscosity - The horizontalViscosity of the fluid.
 * @param speed - The speed at which the Entity sinks
 */
public record FluidEnchantmentHelper(float horizontalViscosity, float speed) {
	/**
	 * @see org.quiltmc.qsl.fluid.mixin.LivingEntityMixin
	 */
	public float getHorizontalViscosity() {
		return horizontalViscosity;
	}

	/**
	 * @see org.quiltmc.qsl.fluid.mixin.LivingEntityMixin
	 */
	public float getSpeed() {
		return speed;
	}
}
