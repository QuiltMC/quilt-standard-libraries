/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC, 2021 QuiltMC
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.tag.api.QuiltTagBuilder;
import org.quiltmc.qsl.tag.impl.QuiltTagHooks;

@Mixin(Tag.Builder.class)
@Implements({@Interface(iface = QuiltTagBuilder.class, prefix = "qtb$")})
public abstract class TagBuilderMixin {
	@Final
	@Shadow
	private List<Tag.TrackedEntry> entries;

	@Shadow
	public abstract Tag.Builder add(Tag.TrackedEntry trackedEntry);

	@Shadow
	public abstract Tag.Builder add(Tag.Entry entry, String source);

	@Shadow
	public abstract Tag.Builder add(Identifier id, String source);

	@Shadow
	public abstract Tag.Builder addOptional(Identifier id, String source);

	@Shadow
	public abstract Tag.Builder addTag(Identifier id, String source);

	@Shadow
	public abstract Tag.Builder addOptionalTag(Identifier id, String source);

	@Shadow
	public abstract Stream<Tag.TrackedEntry> streamEntries();

	@Shadow
	public abstract void forEachTagId(Consumer<Identifier> consumer);

	@Shadow
	public abstract void forEachGroupId(Consumer<Identifier> consumer);

	@Shadow
	public abstract Tag.Builder read(JsonObject json, String source);

	@Shadow
	public abstract <T> Either<Collection<Tag.TrackedEntry>, Tag<T>> build(Function<Identifier, Tag<T>> tagGetter,
	                                                                       Function<Identifier, T> objectGetter);

	@Shadow
	public abstract JsonObject toJson();

	/*
	 * Injections
	 */

	@Unique
	private int quilt$replacementCount;

	@ModifyArg(
			method = "build",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/datafixers/util/Either;right(Ljava/lang/Object;)Lcom/mojang/datafixers/util/Either;",
					remap = false
			)
	)
	private Object onBuild(Object tag) {
		((QuiltTagHooks) tag).quilt$setReplacementCount(this.quilt$replacementCount);
		return tag;
	}

	@Inject(method = "read", at = @At(value = "INVOKE", target = "Ljava/util/List;clear()V"))
	public void onFromJsonClear(JsonObject json, String packName, CallbackInfoReturnable<Tag.Builder> info) {
		this.quilt$replacementCount++;
	}

	/*
	 * QuiltTagBuilder implementation
	 */

	@Intrinsic
	public QuiltTagBuilder qtb$add(Tag.TrackedEntry trackedEntry) {
		return (QuiltTagBuilder) this.add(trackedEntry);
	}

	@Intrinsic
	public QuiltTagBuilder qtb$add(Tag.Entry entry, String source) {
		return (QuiltTagBuilder) this.add(entry, source);
	}

	@Intrinsic
	public QuiltTagBuilder qtb$add(Identifier id, String source) {
		return (QuiltTagBuilder) this.add(id, source);
	}

	@Intrinsic
	public QuiltTagBuilder qtb$addOptional(Identifier id, String source) {
		return (QuiltTagBuilder) this.addOptional(id, source);
	}

	@Intrinsic
	public QuiltTagBuilder qtb$addTag(Identifier id, String source) {
		return (QuiltTagBuilder) this.addTag(id, source);
	}

	@Intrinsic
	public QuiltTagBuilder qtb$addOptionalTag(Identifier id, String source) {
		return (QuiltTagBuilder) this.addOptionalTag(id, source);
	}

	public QuiltTagBuilder qtb$clearEntries() {
		this.entries.clear();
		this.quilt$replacementCount++;
		return (QuiltTagBuilder) this;
	}

	@Intrinsic
	public Stream<Tag.TrackedEntry> qtb$streamEntries() {
		return this.streamEntries();
	}

	@Intrinsic
	public void qtb$visitRequiredDependencies(Consumer<Identifier> consumer) {
		this.forEachTagId(consumer);
	}

	@Intrinsic
	public void qtb$visitOptionalDependencies(Consumer<Identifier> consumer) {
		this.forEachGroupId(consumer);
	}

	@Intrinsic
	public QuiltTagBuilder qtb$read(JsonObject json, String source) {
		return (QuiltTagBuilder) this.read(json, source);
	}

	@Intrinsic
	public <T> Either<Collection<Tag.TrackedEntry>, Tag<T>> qtb$build(Function<Identifier, Tag<T>> tagGetter,
	                                                                  Function<Identifier, T> objectGetter) {
		return this.build(tagGetter, objectGetter);
	}

	@Intrinsic
	public JsonObject qtb$toJson() {
		return this.toJson();
	}
}
