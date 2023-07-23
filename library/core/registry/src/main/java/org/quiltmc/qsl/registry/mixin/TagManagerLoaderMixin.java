package org.quiltmc.qsl.registry.mixin;

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
	private static void quilt$replaceModdedDynamicRegistryTagPath(RegistryKey<? extends Registry<?>> registry, CallbackInfoReturnable<String> cir) {
		Identifier id = registry.getValue();
		if (DynamicMetaRegistryImpl.isModdedRegistryId(id)) {
			cir.setReturnValue("tags/" + id.getNamespace() + "/" + id.getPath());
		}
	}
}
