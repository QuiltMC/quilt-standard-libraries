package org.quiltmc.qsl.component.mixin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.components.NbtComponent;
import org.quiltmc.qsl.component.api.identifier.ComponentIdentifier;
import org.quiltmc.qsl.component.impl.util.StringConstants;
import org.quiltmc.qsl.component.impl.util.duck.NbtComponentProvider;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;

@Implements({
		@Interface(iface = NbtComponentProvider.class, prefix = "nbtExp$"),
		@Interface(iface = ComponentProvider.class, prefix = "comp$")
})
@Mixin(BlockEntity.class)
public class MixinBlockEntity {

	private Map<Identifier, Component> qsl$components;
	private Map<Identifier, NbtComponent<?>> qsl$nbtComponents;


	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
		this.qsl$components = ImmutableMap.copyOf(ComponentProvider.createComponents((ComponentProvider) this));
		this.qsl$nbtComponents = ImmutableMap.copyOf(NbtComponent.getNbtSerializable(this.qsl$components));
	}

	@Inject(method = "toNbt", at = @At("RETURN"))
	private void onWriteNbt(CallbackInfoReturnable<NbtCompound> cir) {
		var rootQslNbt = new NbtCompound();
		this.qsl$nbtComponents.forEach((id, nbtComponent) -> rootQslNbt.put(id.toString(), nbtComponent.write()));

		cir.getReturnValue().put(StringConstants.COMPONENT_ROOT, rootQslNbt);
	}

	@Inject(
			method = "m_qgnqsprj", // The lambda used in second map operation.
			at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;readNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	)
	private static void onReadNbt(NbtCompound nbt, String string, BlockEntity blockEntity, CallbackInfoReturnable<BlockEntity> cir) {
		var rootQslNbt = nbt.getCompound(StringConstants.COMPONENT_ROOT);
		((NbtComponentProvider) blockEntity).get().forEach((id, nbtComponent) -> NbtComponent.forward(nbtComponent, id, rootQslNbt));
	}

	public Map<Identifier, NbtComponent<?>> nbtExp$get() {
		return this.qsl$nbtComponents;
	}

	public Optional<Component> comp$expose(ComponentIdentifier<?> id) {
		return Optional.ofNullable(this.qsl$components.get(id.id()));
	}

	public Map<Identifier, Component> comp$exposeAll() {
		return this.qsl$components;
	}
}
