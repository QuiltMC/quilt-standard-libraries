package org.quiltmc.qsl.registry.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.quiltmc.qsl.base.api.event.ArrayEvent;
import org.quiltmc.qsl.registry.event.api.RegistryEvents;
import org.quiltmc.qsl.registry.event.impl.RegistryEventStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<V> extends Registry<V> implements RegistryEventStorage<V> {
	@Unique
	private final ArrayEvent<RegistryEvents.EntryAdded<V>> quilt$entryAddedEvent = ArrayEvent.create(RegistryEvents.EntryAdded.class, callbacks -> (registry, entry, id, raw) -> {
		for (var callback : callbacks) {
			callback.onAdded(registry, entry, id, raw);
		}
	});

	protected SimpleRegistryMixin(RegistryKey<? extends Registry<V>> key, Lifecycle lifecycle) {
		super(key, lifecycle);
	}

	@Inject(
			method = "set(ILnet/minecraft/util/registry/RegistryKey;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;Z)Ljava/lang/Object;",
			at = @At("HEAD")
	)
	private void quilt$invokeEntryAddEvent(int rawId, RegistryKey<V> key, V entry, Lifecycle lifecycle, boolean checkDuplicateKeys, CallbackInfoReturnable<V> cir) {
		quilt$entryAddedEvent.invoker().onAdded(this, entry, key.getValue(), rawId);
	}

	@Override
	public ArrayEvent<RegistryEvents.EntryAdded<V>> quilt$getEntryAddedEvent() {
		return quilt$entryAddedEvent;
	}
}
