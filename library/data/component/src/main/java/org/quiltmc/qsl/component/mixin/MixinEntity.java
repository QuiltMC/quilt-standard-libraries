package org.quiltmc.qsl.component.mixin;

import com.google.common.collect.ImmutableCollection;
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
import org.quiltmc.qsl.component.impl.ComponentsImpl;
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
import java.util.function.Supplier;

@Mixin(Entity.class)
@Implements(@Interface(iface = ComponentProvider.class, prefix = "comp$"))
public abstract class MixinEntity {

	private ImmutableMap<Identifier, Component> qsl$components;
	private ImmutableMap<Identifier, NbtComponent<?>> qsl$nbtComponents;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onEntityInit(EntityType<?> entityType, World world, CallbackInfo ci) {
		if (!world.isClient) {
			System.out.println(this.getClass()); // TODO: Remove this
			var builder = ImmutableMap.<Identifier, Component>builder();

			Map<Identifier, Supplier<? extends Component>> injections = ComponentsImpl.get((ComponentProvider) this);
			injections.forEach((id, supplier) -> builder.put(id, supplier.get()));
			this.qsl$components = builder.build();
			this.qsl$nbtComponents = this.qsl$components.entrySet().stream()
					.filter(it -> it.getValue() instanceof NbtComponent<?>)
					.collect(
							ImmutableMap::<Identifier, NbtComponent<?>>builder,
							(map, entry) -> map.put(entry.getKey(), ((NbtComponent<?>) entry.getValue())),
							(builder1, builder2) -> builder1.putAll(builder2.build())
					).build();
		} else {
			this.qsl$components = ImmutableMap.of();
			this.qsl$nbtComponents = ImmutableMap.of();
		}
	}

	public Optional<Component> comp$expose(ComponentIdentifier<?> id) {
		return Optional.ofNullable(this.qsl$components.get(id.id()));
	}

	public ImmutableCollection<Component> comp$exposeAll() {
		return this.qsl$components.values();
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
