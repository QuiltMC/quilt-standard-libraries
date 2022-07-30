/*
 * Copyright 2021-2022 QuiltMC
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

package org.quiltmc.qsl.resource.loader.mixin;

import java.util.List;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.resource.NamespaceResourceManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.resource.loader.api.GroupResourcePack;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;

@Mixin(NamespaceResourceManager.class)
public class NamespaceResourceManagerMixin {
	/**
	 * Acts as a pseudo-local variable in {@link NamespaceResourceManager#getAllResources(Identifier)}.
	 * Not thread-safe so a ThreadLocal is required.
	 */
	@Unique
	private final ThreadLocal<List<NamespaceResourceManager.ResourceEntry>> quilt$getAllResources$resources = new ThreadLocal<>();

	@Inject(
			method = "getAllResources",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/resource/NamespaceResourceManager;getMetadataPath(Lnet/minecraft/util/Identifier;)Lnet/minecraft/util/Identifier;"
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onGetAllResources(Identifier id, CallbackInfoReturnable<List<Resource>> cir, List<NamespaceResourceManager.ResourceEntry> resources) {
		this.quilt$getAllResources$resources.set(resources);
	}

	@Redirect(
			method = "getAllResources",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/resource/pack/ResourcePack;contains(Lnet/minecraft/resource/ResourceType;Lnet/minecraft/util/Identifier;)Z"
			)
	)
	private boolean onResourceAdd(ResourcePack pack, ResourceType type, Identifier id) {
		if (pack instanceof GroupResourcePack groupResourcePack) {
			ResourceLoaderImpl.appendResourcesFromGroup((NamespaceResourceManagerAccessor) this, id, groupResourcePack,
					this.quilt$getAllResources$resources.get());
			return false;
		}

		return pack.contains(type, id);
	}

	@Redirect(
			method = "findResourcesOf",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
			),
			allow = 1
	)
	private boolean onResourceAdd(List<NamespaceResourceManager.ResourceEntry> entries, Object entryObject) {
		// Required due to type erasure of List.add
		ResourceEntryAccessor entry = (ResourceEntryAccessor) entryObject;
		ResourcePack pack = entry.getSource();

		if (pack instanceof GroupResourcePack groupResourcePack) {
			ResourceLoaderImpl.appendResourcesFromGroup((NamespaceResourceManagerAccessor) this, entry.getId(),
					groupResourcePack, entries);
			return true;
		}

		return entries.add((NamespaceResourceManager.ResourceEntry) entry);
	}

	@Inject(method = "streamResourcePacks", at = @At("RETURN"), cancellable = true)
	private void onStreamResourcePacks(CallbackInfoReturnable<Stream<ResourcePack>> cir) {
		cir.setReturnValue(cir.getReturnValue()
				.mapMulti((pack, consumer) -> {
					if (pack instanceof GroupResourcePack grouped) {
						grouped.streamPacks().forEach(consumer);
					} else {
						consumer.accept(pack);
					}
				})
		);
	}

	@Mixin(NamespaceResourceManager.ResourceEntry.class)
	public interface ResourceEntryAccessor {
		@Invoker("<init>")
		static NamespaceResourceManager.ResourceEntry create(NamespaceResourceManager parent, Identifier id, Identifier metadataId, ResourcePack source) {
			throw new IllegalStateException("Mixin injection failed.");
		}

		@Accessor
		Identifier getId();

		@Accessor
		ResourcePack getSource();
	}
}
