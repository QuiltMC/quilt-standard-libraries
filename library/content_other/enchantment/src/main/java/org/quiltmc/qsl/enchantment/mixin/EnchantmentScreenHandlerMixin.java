package org.quiltmc.qsl.enchantment.mixin;

import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.enchantment.impl.EnchantmentContext;
import org.quiltmc.qsl.enchantment.impl.EnchantmentGodClass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantmentScreenHandlerMixin extends ScreenHandler {
	@Shadow
	@Final
	private ScreenHandlerContext context;

	private PlayerEntity player;
	private int bookcases;

	// Empty constructor... ignore
	protected EnchantmentScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) { super(type, syncId); }

	@Inject(method = "Lnet/minecraft/screen/EnchantmentScreenHandler;<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At("TAIL"))
	public void capturePlayer(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, CallbackInfo callback) {
		this.player = playerInventory.player;
	}

	@Inject(method = "net/minecraft/screen/EnchantmentScreenHandler.method_17411(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", at = @At(value = "INVOKE", target = "Ljava/util/Random;setSeed(J)V"), locals = LocalCapture.CAPTURE_FAILHARD)
	public void onContentChanged(ItemStack stack, World world, BlockPos pos, CallbackInfo callback, int i) {
		this.bookcases = i;
	}

	@Inject(method = "generateEnchantments", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;generateEnchantments(Ljava/util/Random;Lnet/minecraft/item/ItemStack;IZ)Ljava/util/List;"))
	private void setEnchantmentContext(ItemStack stack, int slot, int level, CallbackInfoReturnable<List<EnchantmentLevelEntry>> callback) {
		this.context.run((world, pos) -> {
			EnchantmentGodClass.context.set(new EnchantmentContext(0, 0, this.bookcases, stack, world, this.player, pos, world.getBlockState(pos), world.getBlockEntity(pos)));
		});
	}

	@Inject(method = "generateEnchantments", at = @At("RETURN"))
	private void clearEnchantmentContext(ItemStack stack, int slot, int level, CallbackInfoReturnable<List<EnchantmentLevelEntry>> callback) {
		EnchantmentGodClass.context.remove();
	}
}
