/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.tag.TagGroup;
import net.minecraft.tag.TagManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import org.quiltmc.qsl.tag.impl.QuiltTagManagerHooks;

@Mixin(TagManager.class)
public class TagManagerMixin implements QuiltTagManagerHooks {
	@Shadow
	@Mutable
	@Final
	private Map<RegistryKey<? extends Registry<?>>, TagGroup<?>> tagGroups;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(Map<RegistryKey<? extends Registry<?>>, TagGroup<?>> tagGroups, CallbackInfo ci) {
		// Make it mutable, so we can add dynamic registry tags later.
		this.tagGroups = new Object2ObjectOpenHashMap<>(tagGroups);
	}

	@Override
	public void quilt$putTagGroup(RegistryKey<? extends Registry<?>> registryKey, TagGroup<?> tagGroup) {
		this.tagGroups.put(registryKey, tagGroup);
	}
}
