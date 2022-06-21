package org.quiltmc.qsl.component.mixin;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import org.quiltmc.qsl.component.api.components.NbtComponent;
import org.quiltmc.qsl.component.impl.util.StringConstants;
import org.quiltmc.qsl.component.impl.util.duck.NbtComponentProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PacketByteBuf.class)
public abstract class MixinPacketByteBuf extends ByteBuf {
	@Inject(method = "readItemStack", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
	private void readComponentData(CallbackInfoReturnable<ItemStack> cir) {
		ItemStack ret = cir.getReturnValue();
		var this$ = (PacketByteBuf) (Object) this;
		NbtCompound rootQslNbt = this$.readNbt();
		if (rootQslNbt != null) {
			((NbtComponentProvider) (Object) ret).getNbtComponents().forEach((id, component) -> NbtComponent.readFrom(component, id, rootQslNbt));
			cir.setReturnValue(ret);
		}
	}

	@Inject(method = "writeItemStack", at = @At("RETURN"))
	private void writeComponentData(ItemStack stack, CallbackInfoReturnable<PacketByteBuf> cir) {
		var this$ = (PacketByteBuf) (Object) this;
		this$.writeNbt(stack.writeNbt(new NbtCompound()).getCompound(StringConstants.COMPONENT_ROOT));
	}
}
