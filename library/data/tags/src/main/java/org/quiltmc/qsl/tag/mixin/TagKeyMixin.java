/*
 * Copyright 2022 The Quilt Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.tag.mixin;

import java.util.Objects;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.tag.api.QuiltTagKey;
import org.quiltmc.qsl.tag.api.TagType;
import org.quiltmc.qsl.tag.impl.QuiltTagKeyHooks;

@Mixin(TagKey.class)
public class TagKeyMixin<T> implements QuiltTagKey<T>, QuiltTagKeyHooks {
	@Shadow
	@Final
	private RegistryKey<? extends Registry<T>> registry;
	@Shadow
	@Final
	private Identifier id;
	@Unique
	private TagType quilt$type;

	@Inject(method = "<init>", at = @At("TAIL"))
	public void onInit(RegistryKey<? extends Registry<T>> registryKey, Identifier identifier, CallbackInfo ci) {
		this.quilt$setType(TagType.NORMAL);
	}

	@Override
	public void quilt$setType(TagType type) {
		this.quilt$type = type;
	}

	@Override
	public TagType type() {
		return this.quilt$type;
	}

	@SuppressWarnings("unchecked")
	@Inject(method = "equals", at = @At("RETURN"), cancellable = true)
	private void onEquals(Object o, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ()) {
			if (this.type() != ((QuiltTagKey<T>) o).type()) {
				cir.setReturnValue(false);
			}
		}
	}

	@Inject(method = "toString", at = @At("RETURN"), cancellable = true)
	private void onToString(CallbackInfoReturnable<String> cir) {
		cir.setReturnValue(cir.getReturnValue() + "{type: " + this.type() + "}");
	}

	/**
	 * @author The Quilt Project, LambdAurora
	 * @reason replace hash code to hash the tag type too
	 */
	@Overwrite
	public final int hashCode() {
		return Objects.hash(this.registry, this.id, this.type());
	}
}
