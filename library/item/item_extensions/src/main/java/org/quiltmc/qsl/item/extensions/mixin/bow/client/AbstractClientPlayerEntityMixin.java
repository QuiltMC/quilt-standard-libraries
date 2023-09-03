package org.quiltmc.qsl.item.extensions.mixin.bow.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import org.quiltmc.qsl.item.extensions.api.bow.BowExtensions;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin {
	// Make sure that the fov is changed for custom items
	@Redirect(
		  method = "getSpeed",
		  at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z")
	)
	private boolean renderFov(ItemStack instance, Item item) {
		if (item == Items.BOW) {
			return instance.getItem() instanceof BowExtensions; // Return bow for fov
		}
		return instance.isOf(item); // Default behavior
	}
}
