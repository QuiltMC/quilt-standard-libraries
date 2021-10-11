/*
 * Copyright 2021 QuiltMC
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

import java.util.Collection;
import java.util.List;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.tag.Tag;

import org.quiltmc.qsl.tag.api.QuiltTagBuilder;
import org.quiltmc.qsl.tag.impl.QuiltTagHooks;

@Mixin(Tag.Builder.class)
public class TagBuilderMixin implements QuiltTagBuilder {
	@Final
	@Shadow
	private List<Tag.TrackedEntry> entries;

	@Unique
	private int quilt$clearCount;

	@Redirect(
			method = "build",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/datafixers/util/Either;right(Ljava/lang/Object;)Lcom/mojang/datafixers/util/Either;",
					remap = false
			)
	)
	private Either<Collection<Tag.TrackedEntry>, Object> build(Object tag) {
		((QuiltTagHooks) tag).quilt$setClearCount(this.quilt$clearCount);
		return Either.right(tag);
	}

	@Inject(method = "read", at = @At(value = "INVOKE", target = "Ljava/util/List;clear()V"))
	public void onFromJsonClear(JsonObject json, String packName, CallbackInfoReturnable<Tag.Builder> info) {
		this.quilt$clearCount++;
	}

	@Override
	public void clearEntries() {
		this.entries.clear();
		this.quilt$clearCount++;
	}
}
