package org.quiltmc.qsl.component.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.component.api.ComponentContainer;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.impl.LazifiedComponentContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntity.class)
public abstract class MixinBlockEntity implements ComponentProvider {

	private ComponentContainer qsl$container;

	@Inject(method = "readNbt", at = @At("TAIL"))
	private void onReadNbt(NbtCompound nbt, CallbackInfo ci) {
		this.qsl$container.readNbt(nbt);
	}

	@Shadow
	public abstract void markDirty();

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
		this.qsl$container = LazifiedComponentContainer.create(this).orElseThrow();
		this.qsl$container.setSaveOperation(this::markDirty);
	}

	@Inject(method = "toNbt", at = @At("TAIL"))
	private void onWriteNbt(CallbackInfoReturnable<NbtCompound> cir) {
		this.qsl$container.writeNbt(cir.getReturnValue());
	}

	@Override
	public @NotNull ComponentContainer getContainer() {
		return this.qsl$container;
	}
}
