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

package org.quiltmc.qsl.component.mixin;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.SaveVersionInfo;
import net.minecraft.world.timer.Timer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.ComponentContainer;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.impl.container.LazifiedComponentContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.UUID;

@Mixin(LevelProperties.class)
public abstract class MixinLevelProperties implements ServerWorldProperties, SaveProperties, ComponentProvider {

	private ComponentContainer qsl$container;

	@Inject(method = "readProperties", at = @At("RETURN"))
	private static void readComponentData(Dynamic<NbtElement> dynamic, DataFixer dataFixer, int dataVersion, @Nullable NbtCompound playerData, LevelInfo levelInfo, SaveVersionInfo saveVersionInfo, GeneratorOptions generatorOptions, Lifecycle lifecycle, CallbackInfoReturnable<LevelProperties> cir) {
		cir.getReturnValue().getContainer().readNbt((NbtCompound) dynamic.getValue());
	}

	@Inject(method = "<init>(Lcom/mojang/datafixers/DataFixer;ILnet/minecraft/nbt/NbtCompound;ZIIIFJJIIIZIZZZLnet/minecraft/world/border/WorldBorder$Properties;IILjava/util/UUID;Ljava/util/Set;Lnet/minecraft/world/timer/Timer;Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/world/level/LevelInfo;Lnet/minecraft/world/gen/GeneratorOptions;Lcom/mojang/serialization/Lifecycle;)V", at = @At("TAIL"))
	private void onInit(DataFixer dataFixer, int i, NbtCompound nbtCompound, boolean bl, int j, int k, int l, float f, long m, long n, int o, int p, int q, boolean bl2, int r, boolean bl3, boolean bl4, boolean bl5, WorldBorder.Properties properties, int s, int t, UUID uUID, Set<String> set, Timer<MinecraftServer> timer, NbtCompound nbtCompound2, NbtCompound nbtCompound3, LevelInfo levelInfo, GeneratorOptions generatorOptions, Lifecycle lifecycle, CallbackInfo ci) {
		this.qsl$container = LazifiedComponentContainer.create(this).orElseThrow();
	}

	@Inject(method = "updateProperties", at = @At("TAIL"))
	private void writeComponentData(DynamicRegistryManager registryManager, NbtCompound levelNbt, NbtCompound playerNbt, CallbackInfo ci) {
		this.qsl$container.writeNbt(levelNbt);
	}

	@Override
	public @NotNull ComponentContainer getContainer() {
		return this.qsl$container;
	}
}
