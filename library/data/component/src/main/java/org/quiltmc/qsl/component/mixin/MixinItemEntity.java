package org.quiltmc.qsl.component.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity extends Entity {
	public MixinItemEntity(EntityType<?> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "canMerge(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
	private static void properCanMerge(ItemStack stack1, ItemStack stack2, CallbackInfoReturnable<Boolean> cir) {
		// data get entity @e[type=item, limit=1]
		cir.setReturnValue(ItemStack.canCombine(stack1, stack2) && stack1.getCount() + stack2.getCount() <= stack1.getMaxCount());
	}

}
