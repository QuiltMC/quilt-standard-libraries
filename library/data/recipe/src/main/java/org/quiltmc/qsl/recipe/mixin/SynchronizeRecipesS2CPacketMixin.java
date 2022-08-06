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

package org.quiltmc.qsl.recipe.mixin;

import java.util.Collection;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.Recipe;

import org.quiltmc.qsl.recipe.impl.RecipeImpl;

// TODO Conditionally remove only for vanilla clients
/**
 * Removes the quilt brewing recipes from client sync packet.
 * <p>
 * This is fine because they are only used serverside.
 */
@Mixin(SynchronizeRecipesS2CPacket.class)
public class SynchronizeRecipesS2CPacketMixin {
	@Shadow
	@Final
	private List<Recipe<?>> recipes;

	@Inject(method = "<init>(Ljava/util/Collection;)V", at = @At("TAIL"))
	private void stripBrewingRecipes(Collection<Recipe<?>> collection, CallbackInfo ci) {
		this.recipes.removeIf(recipe -> recipe.getType() == RecipeImpl.BREWING);
	}
}