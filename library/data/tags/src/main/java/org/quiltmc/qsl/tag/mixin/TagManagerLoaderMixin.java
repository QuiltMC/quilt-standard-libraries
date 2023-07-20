package org.quiltmc.qsl.tag.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagManagerLoader;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.registry.impl.dynamic.DynamicMetaRegistryImpl;

@Mixin(TagManagerLoader.class)
public class TagManagerLoaderMixin {
	@Inject(method = "getRegistryDirectory", at = @At("HEAD"), cancellable = true)
	private static void onGetRegistryDirectory(RegistryKey<? extends Registry<?>> registry, CallbackInfoReturnable<String> cir) {
		/*
		In the off-chance a tag registry is added via the dynamic registry api (instead of using the Tags API),
		this appends tags/ to the start of the registry's directory.

		This primarily exists for compat with FAPI, but will also force more standard
		behavior on QSL mods not using the tags API.
		*/
		Identifier id = registry.getValue();
		if (DynamicMetaRegistryImpl.isModdedRegistryId(id) && !cir.getReturnValue().startsWith("tags/")) {
			cir.setReturnValue("tags/" + id.getNamespace() + "/" + id.getPath());
		}
	}
}
