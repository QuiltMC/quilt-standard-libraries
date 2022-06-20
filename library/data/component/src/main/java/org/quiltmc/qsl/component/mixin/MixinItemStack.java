package org.quiltmc.qsl.component.mixin;

import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
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
		@Interface(iface = ComponentProvider.class, prefix = "comp$"),
		@Interface(iface = NbtComponentProvider.class, prefix = "nbtExp$")
})
@Mixin(ItemStack.class)
public abstract class MixinItemStack { // FIXME: Figure out why ItemEntities erase the Qsl Component data once they get picked up??!!

	private Map<Identifier, Component> qsl$components;
	private Map<Identifier, NbtComponent<?>> qsl$nbtComponents;

	@Inject(method = "canCombine", at = @At("RETURN"), cancellable = true)
	private static void checkForComponents(ItemStack stack, ItemStack otherStack, CallbackInfoReturnable<Boolean> cir) {
		var otherMap = ((ComponentProvider) (Object) otherStack).exposeAll();
		cir.setReturnValue(cir.getReturnValue() && ((ComponentProvider) (Object) stack).exposeAll().entrySet().stream()
				.allMatch(entry -> otherMap.get(entry.getKey()).equals(entry.getValue()))
		);
	}

	@Inject(method = "isEqual", at = @At("RETURN"), cancellable = true)
	private void checkForComponentsClient(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		var otherMap = ((ComponentProvider) (Object) stack).exposeAll();
		cir.setReturnValue(cir.getReturnValue() && ((ComponentProvider) this).exposeAll().entrySet().stream()
				.allMatch(entry -> otherMap.get(entry.getKey()).equals(entry.getValue()))
		);
	}

	@Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;I)V", at = @At("RETURN"))
	private void onItemInit(ItemConvertible itemConvertible, int i, CallbackInfo ci) {
		this.qsl$components = ComponentProvider.createComponents((ComponentProvider) this);
		this.qsl$nbtComponents = NbtComponent.getNbtSerializable(this.qsl$components);
	}

	@Inject(method = "<init>(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("RETURN"))
	private void deserializeComponentsFromNbt(NbtCompound nbtCompound, CallbackInfo ci) {
		this.qsl$components = ComponentProvider.createComponents((ComponentProvider) this);
		this.qsl$nbtComponents = NbtComponent.getNbtSerializable(this.qsl$components);
		var rootQslNbt = nbtCompound.getCompound(StringConstants.COMPONENT_ROOT);
		this.qsl$nbtComponents.forEach((id, nbtComponent) -> NbtComponent.readFrom(nbtComponent, id, rootQslNbt));
	}

	@Inject(method = "writeNbt", at = @At("RETURN"))
	private void onWrite(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
		var rootQslNbt = new NbtCompound();
		this.qsl$nbtComponents.forEach((id, nbtComponent) -> rootQslNbt.put(id.toString(), nbtComponent.write()));
		nbt.put(StringConstants.COMPONENT_ROOT, rootQslNbt);
	}

	@Inject(method = "copy", at = @At(value = "RETURN", ordinal = 1))
	private void recordComponentNbt(CallbackInfoReturnable<ItemStack> cir) {
		ItemStack stack = cir.getReturnValue();
		Map<Identifier, NbtComponent<?>> stackMap = ((NbtComponentProvider) (Object) stack).getNbtComponents();
		for (var entry : this.qsl$nbtComponents.entrySet()) {
			NbtElement nbt = entry.getValue().write();
			Identifier id = entry.getKey();

			NbtComponent.read(stackMap.get(id), nbt);
		}
	}

	public Optional<Component> comp$expose(ComponentIdentifier<?> id) {
		return Optional.ofNullable(this.qsl$components.get(id.id()));
	}

	public Map<Identifier, Component> comp$exposeAll() {
		return this.qsl$components;
	}

	public Map<Identifier, NbtComponent<?>> nbtExp$getNbtComponents() {
		return this.qsl$nbtComponents;
	}
}
