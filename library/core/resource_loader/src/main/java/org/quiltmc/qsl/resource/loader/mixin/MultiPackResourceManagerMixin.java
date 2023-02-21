/*
 * Copyright 2021-2023 QuiltMC
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resource.MultiPackResourceManager;
import net.minecraft.resource.NamespaceResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.resource.pack.metadata.ResourceFilterMetadata;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.resource.loader.api.GroupResourcePack;
import org.quiltmc.qsl.resource.loader.impl.QuiltMultiPackResourceManagerHooks;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;

@Mixin(MultiPackResourceManager.class)
public abstract class MultiPackResourceManagerMixin implements QuiltMultiPackResourceManagerHooks {
	@Mutable
	@Shadow
	@Final
	private List<ResourcePack> packs;

	@Shadow
	@Final
	private Map<String, NamespaceResourceManager> namespaceManagers;

	@Shadow
	@Nullable
	protected abstract ResourceFilterMetadata getFilter(ResourcePack pack);

	@Unique
	private /*final*/ ResourceType quilt$type;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(ResourceType type, List<ResourcePack> packs, CallbackInfo ci) {
		this.quilt$type = type;

		if (packs instanceof ArrayList<ResourcePack>) {
			this.packs = packs; // Make the list mutable.
		}
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

	@SuppressWarnings("ConstantConditions")
	@Override
	public void quilt$appendTopPacks() {
		if (!(this.packs instanceof ArrayList<ResourcePack>)) {
			this.packs = new ArrayList<>(this.packs);
		}

		ResourceLoaderImpl.get(this.quilt$type).appendTopPacks((MultiPackResourceManager) (Object) this, this.packs::add);
	}

	@Override
	public void quilt$recomputeNamespaces() {
		this.namespaceManagers.clear();
		List<String> namespaces = this.packs.stream().flatMap(pack -> pack.getNamespaces(this.quilt$type).stream()).distinct().toList();

		for (var pack : this.packs) {
			ResourceFilterMetadata resourceFilterMetadata = this.getFilter(pack);
			Set<String> set = pack.getNamespaces(this.quilt$type);
			Predicate<Identifier> predicate = resourceFilterMetadata != null ? id -> resourceFilterMetadata.matchPath(id.getPath()) : null;

			for (var namespace : namespaces) {
				boolean hasNamespace = set.contains(namespace);
				boolean namespaceFilter = resourceFilterMetadata != null && resourceFilterMetadata.matchNamespace(namespace);

				if (hasNamespace || namespaceFilter) {
					NamespaceResourceManager namespaceResourceManager = this.namespaceManagers.computeIfAbsent(namespace,
							n -> new NamespaceResourceManager(this.quilt$type, n));

					if (hasNamespace && namespaceFilter) {
						namespaceResourceManager.addPack(pack, predicate);
					} else if (hasNamespace) {
						namespaceResourceManager.addPack(pack);
					} else {
						namespaceResourceManager.addPack(pack.getName(), predicate);
					}
				}
			}
		}
	}
}
