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
 * This is fine because they are only used serverside.</p>
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
