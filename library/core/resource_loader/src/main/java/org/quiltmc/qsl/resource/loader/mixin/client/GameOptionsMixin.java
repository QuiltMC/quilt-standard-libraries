/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.resource.loader.mixin.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.option.GameOptions;
import net.minecraft.resource.pack.ResourcePackManager;

import org.quiltmc.loader.api.minecraft.ClientOnly;

@ClientOnly
@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {
	@Shadow
	public List<String> resourcePacks;

	@Shadow
	private static List<String> parseList(String content) {
		throw new IllegalStateException("Injection failed.");
	}

	@Shadow
	@Final
	static Gson GSON;

	/**
	 * Represents the available resource packs, similar to how data packs work.
	 * This allow to keep track of resource packs that are present but forcefully disabled for built-in resource packs.
	 */
	@Unique
	private List<String> quilt$availableResourcePacks = new ArrayList<>();

	@Inject(method = "accept(Lnet/minecraft/client/option/GameOptions$Visitor;)V", at = @At("HEAD"))
	private void onAccept(GameOptions.Visitor visitor, CallbackInfo ci) {
		this.quilt$availableResourcePacks = visitor.visitObject("quilt_available_resource_packs",
				this.quilt$availableResourcePacks, GameOptionsMixin::parseList, GSON::toJson);
	}

	@Inject(method = "addResourcePackProfilesToManager", at = @At("HEAD"))
	private void onAddResourcePackProfilesToManager(ResourcePackManager manager, CallbackInfo ci) {
		var toEnable = new ArrayList<String>();

		// Remove all resource packs that cannot be found from the available resource packs list.
		this.quilt$availableResourcePacks.removeIf(availableResourcePack -> manager.getProfile(availableResourcePack) == null);

		// Update available resource packs.
		for (var profile : manager.getProfiles()) {
			if (!this.quilt$availableResourcePacks.contains(profile.getName())) {
				if (profile.getActivationType().isEnabledByDefault()) {
					toEnable.add(profile.getName());
				}

				this.quilt$availableResourcePacks.add(profile.getName());
			}
		}

		this.resourcePacks.addAll(toEnable);
	}
}
