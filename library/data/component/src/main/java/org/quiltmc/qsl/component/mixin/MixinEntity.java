package org.quiltmc.qsl.component.mixin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.components.NbtComponent;
import org.quiltmc.qsl.component.api.identifier.ComponentIdentifier;
import org.quiltmc.qsl.component.impl.util.StringConstants;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;

@Mixin(Entity.class)
@Implements(@Interface(iface = ComponentProvider.class, prefix = "comp$"))
public abstract class MixinEntity {

	private Map<Identifier, Component> qsl$components;
	private Map<Identifier, NbtComponent<?>> qsl$nbtComponents;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onEntityInit(EntityType<?> entityType, World world, CallbackInfo ci) {
		this.qsl$components = ImmutableMap.copyOf(ComponentProvider.createComponents((ComponentProvider) this));
		this.qsl$nbtComponents = ImmutableMap.copyOf(NbtComponent.getNbtSerializable(this.qsl$components));
	}

	public Optional<Component> comp$expose(ComponentIdentifier<?> id) {
		return Optional.ofNullable(this.qsl$components.get(id.id()));
	}

	public Map<Identifier, Component> comp$exposeAll() {
		return this.qsl$components;
	}

	@Inject(method = "writeNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V"))
	private void onSerialize(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
		var rootQslNbt = new NbtCompound();
		this.qsl$nbtComponents.forEach((id, nbtComponent) -> rootQslNbt.put(id.toString(), nbtComponent.write()));

		nbt.put(StringConstants.COMPONENT_ROOT, rootQslNbt);
	}

	@Inject(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V"))
	private void onDeserialize(NbtCompound nbt, CallbackInfo ci) {
		var rootQslNbt = nbt.getCompound(StringConstants.COMPONENT_ROOT);
		this.qsl$nbtComponents.forEach((id, nbtComponent) -> NbtComponent.forward(nbtComponent, id, rootQslNbt));
	}
}
