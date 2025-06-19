package org.quiltmc.qsl.worldgen.biome.mixin.modification;

import org.quiltmc.qsl.worldgen.biome.impl.modification.BiomeModificationImpl;

import net.minecraft.registry.DynamicRegistrySync;
import net.minecraft.registry.Holder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryLoader;

import com.mojang.serialization.DynamicOps;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.List;
import java.util.Set;

@Mixin(DynamicRegistrySync.class)
abstract class DynamicRegistrySyncMixin {
	@ModifyExpressionValue(
		// method_56595 is the Consumer<Holder> lambda passed to registry.streamHolders().forEach(...) in serialize
		method = "method_56595",
		slice = @Slice(from = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/registry/Registry;getRegistrationInfo(Lnet/minecraft/registry/RegistryKey;)" +
				"Ljava/util/Optional;"
		)),
		at = @At(
			value = "INVOKE", ordinal = 0,
			target = "Ljava/util/Optional;isPresent()Z"
		)
	)
	private static boolean andNotModified(
		boolean fromKnowPack, Registry<?> registry, Set<?> knownPacks, RegistryLoader.DecodingData<?> data,
		DynamicOps<?> ops, List<?> entries, Holder.Reference<?> holder
	) {
		return fromKnowPack && holder.getKey().filter(BiomeModificationImpl::wasModified).isEmpty();
	}
}
