package org.quiltmc.qsl.component.mixin;

import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.ComponentContainer;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.container.LazifiedComponentContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("ConstantConditions")
@Mixin(ItemStack.class)
public abstract class MixinItemStack implements ComponentProvider { // TODO: Make sure nothing else may be broken before final PR.

	@Shadow
	public abstract @Nullable NbtCompound getNbt();

	@Shadow
	public abstract NbtCompound getOrCreateNbt();

	private LazifiedComponentContainer qsl$container;

	@Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;I)V", at = @At("TAIL"))
	private void initContainer(ItemConvertible itemConvertible, int i, CallbackInfo ci) {
		this.qsl$container = LazifiedComponentContainer.create(this).orElseThrow();
		this.qsl$container.setSaveOperation(() -> this.qsl$container.writeNbt(this.getOrCreateNbt()));
	}

	@Inject(method = "<init>(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("TAIL"))
	private void readContainer(NbtCompound nbtCompound, CallbackInfo ci) {
		this.qsl$container = LazifiedComponentContainer.create(this).orElseThrow();
		this.qsl$container.setSaveOperation(() -> this.qsl$container.writeNbt(this.getOrCreateNbt()));
		this.qsl$container.readNbt(this.getNbt());
	}

	@Inject(method = "setNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;postProcessNbt(Lnet/minecraft/nbt/NbtCompound;)V"))
	private void readContainerAgain(NbtCompound nbt, CallbackInfo ci) {
		if (this.qsl$container != null) {
			this.qsl$container.readNbt(this.getNbt());
		}
	}

	@Inject(method = "copy", at = @At(value = "RETURN", ordinal = 1))
	private void deserializeContainer(CallbackInfoReturnable<ItemStack> cir) {
		var container = cir.getReturnValue().getContainer();
		container.readNbt(this.getOrCreateNbt());
	}

	@Override
	public @NotNull ComponentContainer getContainer() {
		return this.qsl$container;
	}
}
