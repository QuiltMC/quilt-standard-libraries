package org.quiltmc.qsl.tag.mixin;

import org.quiltmc.qsl.tag.impl.TagRegistryImpl;

import net.minecraft.registry.Holder;
import net.minecraft.registry.HolderSet;
import net.minecraft.registry.tag.TagKey;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Map;

// C_hicdurkx is the anonymous Registry.PendingTags implementation in SimpleRegistry::startTagReload
@Mixin(targets = {"net.minecraft.registry.SimpleRegistry$C_hicdurkx"})
abstract class SimpleRegistryPendingTagsMixin {
	@WrapOperation(
		// method_63536 is the lambda in bind() passed to Map::forEach
		method = "method_63536",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/HolderSet$NamedSet;bindTo(Ljava/util/List;)V")
	)
	private static void populateTag(
		HolderSet.NamedSet<?> instance, List<Holder<?>> contents, Operation<Void> original,
		Map<TagKey<?>, HolderSet. NamedSet<?>> tags, TagKey<?> key
	) {
		original.call(instance, contents);
		TagRegistryImpl.populateTag(key, contents);
	}
}
