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

package org.quiltmc.qsl.tag.mixin.client;

import java.util.Collection;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.registry.Holder;
import net.minecraft.registry.tag.TagKey;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.tag.api.QuiltTagKey;
import org.quiltmc.qsl.tag.api.TagType;
import org.quiltmc.qsl.tag.impl.client.QuiltHolderReferenceHooks;

@ClientOnly
@Mixin(Holder.Reference.class)
public abstract class HolderReferenceMixin<T> implements Holder<T>, QuiltHolderReferenceHooks<T> {
	@Unique
	private Set<TagKey<T>> quilt$fallbackTags = Set.of();
	@Unique
	private Set<TagKey<T>> quilt$clientTags = Set.of();

	@SuppressWarnings({"unchecked", "RedundantCast"})
	@Inject(method = "isIn", at = @At("HEAD"), cancellable = true)
	private void onIsInStart(TagKey<T> tag, CallbackInfoReturnable<Boolean> cir) {
		if (((QuiltTagKey<T>) (Object) tag).type() == TagType.CLIENT_ONLY) {
			cir.setReturnValue(this.quilt$clientTags.contains(tag));
		}
	}

	@SuppressWarnings({"unchecked", "RedundantCast"})
	@Inject(method = "isIn", at = @At("RETURN"), cancellable = true)
	private void onIsInEnd(TagKey<T> tag, CallbackInfoReturnable<Boolean> cir) {
		if (((QuiltTagKey<T>) (Object) tag).type() == TagType.CLIENT_FALLBACK && !cir.getReturnValueZ()) {
			cir.setReturnValue(this.quilt$fallbackTags.contains(tag));
		}
	}

	@Override
	public void quilt$setFallbackTags(Collection<TagKey<T>> tags) {
		this.quilt$fallbackTags = Set.copyOf(tags);
	}

	@Override
	public void quilt$setClientTags(Collection<TagKey<T>> tags) {
		this.quilt$clientTags = Set.copyOf(tags);
	}
}
