package org.quiltmc.qsl.component.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.component.api.ComponentContainer;
import org.quiltmc.qsl.component.impl.LazifiedComponentContainer;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Implements(@Interface(iface = ComponentProvider.class, prefix = "comp$"))
@Mixin(BlockEntity.class)
public abstract class MixinBlockEntity {

	private ComponentContainer qsl$container;

	@Inject(
			method = "m_qgnqsprj", // The lambda used in second map operation.
			at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;readNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	)
	private static void onReadNbt(NbtCompound nbt, String string, BlockEntity blockEntity, CallbackInfoReturnable<BlockEntity> cir) {
		((ComponentProvider) blockEntity).getContainer().readNbt(nbt);
	}

	@Shadow
	public abstract void markDirty();

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
		this.qsl$container = LazifiedComponentContainer.create(this).orElseThrow();
		this.qsl$container.setSaveOperation(this::markDirty);
	}

	@Inject(method = "toNbt", at = @At("RETURN"))
	private void onWriteNbt(CallbackInfoReturnable<NbtCompound> cir) {
		this.qsl$container.writeNbt(cir.getReturnValue());
	}

	@NotNull
	public ComponentContainer comp$getContainer() {
		return this.qsl$container;
	}
}
