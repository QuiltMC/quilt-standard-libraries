package org.quiltmc.qsl.component.mixin;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.*;
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

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Mixin(Entity.class)
@Implements(@Interface(iface = ComponentProvider.class, prefix = "comp$"))
public abstract class MixinEntity {

	private ImmutableMap<Identifier, Component> qsl$components;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onEntityInit(EntityType<?> entityType, World world, CallbackInfo ci) {
		if (!world.isClient) {
			var this$ = (Entity) (Object) this;
			System.out.println(this.getClass());
			var builder = ImmutableMap.<Identifier, Component>builder();

			Map<Identifier, Supplier<? extends Component>> injections =
					ComponentsImpl.get((ComponentProvider) this$);
			injections.forEach((id, supplier) -> builder.put(id, supplier.get()));
			this.qsl$components = builder.build();
		}
	}

	public <T extends Component> Optional<T> comp$expose(ComponentIdentifier<T> id) {
		return Optional.ofNullable(this.qsl$components.get(id.id()))
				.map(id::cast)
				.map(Optional::orElseThrow);
	}

	public ImmutableCollection<Component> comp$exposeAll() {
		return this.qsl$components.values();
	}

	@Inject(method = "writeNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V"))
	private void onSerialize(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
		var componentNbt = new NbtCompound();
		this.qsl$applyActionToSerializable((id, nbtComponent) -> componentNbt.put(id.toString(), nbtComponent.write()));

		nbt.put(StringConstants.COMPONENT_ROOT, componentNbt);
	}

	@Inject(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V"))
	private void onDeserialize(NbtCompound nbt, CallbackInfo ci) {
		var rootQslNbt = nbt.getCompound(StringConstants.COMPONENT_ROOT);
		this.qsl$applyActionToSerializable((id, nbtComponent) -> NbtComponent.forward(nbtComponent, id, rootQslNbt));
	}

	private void qsl$applyActionToSerializable(BiConsumer<Identifier, NbtComponent<?>> action) {
		this.qsl$components.entrySet().stream()
				.filter(it -> it.getValue() instanceof NbtComponent<?>)
				.map(it -> Pair.of(it.getKey(), ((NbtComponent<?>) it.getValue())))
				.forEach(pair -> action.accept(pair.key(), pair.value()));
	}
}
