/*
 * Copyright 2022 QuiltMC
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
package org.quiltmc.qsl.registry.mixin.client;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.registry.impl.sync.client.RebuildableIdModelHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin implements RebuildableIdModelHolder {

	@Shadow
	@Final
	private Int2ObjectMap<ParticleFactory<?>> factories;
	@Unique
	private Map<ParticleType<?>, ParticleFactory<?>> quilt$factoryMap = new Object2ObjectOpenHashMap<>();

	@Coerce
	@Inject(method = "registerFactory(Lnet/minecraft/particle/ParticleType;Lnet/minecraft/client/particle/ParticleManager$SpriteAwareFactory;)V", at = @At("TAIL"))
	private void quilt$storeFactory(ParticleType<?> type, @Coerce Object factory, CallbackInfo ci) {
		this.quilt$factoryMap.put(type, this.factories.get(Registry.PARTICLE_TYPE.getRawId(type)));
	}

	@Inject(method = "registerFactory(Lnet/minecraft/particle/ParticleType;Lnet/minecraft/client/particle/ParticleFactory;)V", at = @At("TAIL"))
	private void quilt$storeFactory(ParticleType<?> type, ParticleFactory<?> factory, CallbackInfo ci) {
		this.quilt$factoryMap.put(type, factory);
	}

	@Override
	public void quilt$rebuildIds() {
		this.factories.clear();

		for (var entry : this.quilt$factoryMap.entrySet()) {
			this.factories.put(Registry.PARTICLE_TYPE.getRawId(entry.getKey()), entry.getValue());
		}
	}
}
