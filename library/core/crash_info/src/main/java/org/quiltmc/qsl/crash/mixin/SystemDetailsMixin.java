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

package org.quiltmc.qsl.crash.mixin;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.util.SystemDetails;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

@Mixin(SystemDetails.class)
abstract class SystemDetailsMixin {
	@Shadow
	public abstract void addSection(String name, Supplier<String> valueSupplier);

	/**
	 * Adds a section to the system details listing all Quilt mods which are present.
	 */
	@Inject(method = "<init>", at = @At("TAIL"))
	private void addQuiltMods(CallbackInfo info) {
		this.addSection("Quilt Mods", () -> {
			StringBuilder builder = new StringBuilder();

			for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
				var metadata = mod.getMetadata();

				builder.append("\n\t\t%s: %s %s".formatted(
						metadata.getId(),
						metadata.getName(),
						metadata.getVersion().getFriendlyString())
				);
			}

			return builder.toString();
		});

		// If we wish to add any additional sections, this is a great place to do so.
	}
}
