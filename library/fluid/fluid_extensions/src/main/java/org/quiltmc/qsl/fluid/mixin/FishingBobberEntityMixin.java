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

package org.quiltmc.qsl.fluid.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.quiltmc.qsl.fluid.api.CustomFluidInteracting;
import org.quiltmc.qsl.fluid.api.FishingBobberEntityExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin implements CustomFluidInteracting, FishingBobberEntityExtensions {

	@Redirect(method = "tick", at = @At( value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/tag/TagKey;)Z", ordinal = 0))
	public boolean tick(FluidState instance, TagKey<Fluid> tag) {
		return instance.isIn(this.canFishingbobberSwimOn());
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/tag/TagKey;)Z",ordinal = 1))
	public boolean tick2(FluidState instance, TagKey<Fluid> tag) {
		return instance.isIn(this.canFishingbobberSwimOn());
	}

	@Inject(method = "tickFishingLogic", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;up()Lnet/minecraft/util/math/BlockPos;"), locals = LocalCapture.CAPTURE_FAILSOFT)
	public void canFish(BlockPos pos, CallbackInfo ci, ServerWorld serverWorld, int i) {
		Fluid fluid = serverWorld.getBlockState(pos).getFluidState().getFluid();
		if(!fluid.isIn(this.canFishingbobberCatchIn())) {
			ci.cancel();
		}
	}

	@Redirect(method = "tickFishingLogic", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
	public boolean spawnParticles(BlockState instance, Block block) {
		return instance.getFluidState().isIn(canFishingbobberCatchIn());
	}

	@Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/LootManager;getTable(Lnet/minecraft/util/Identifier;)Lnet/minecraft/loot/LootTable;"))
	public LootTable changeLootTable(LootManager instance, Identifier id) {
		return ((FishingBobberEntity)(Object)this).world.getServer().getLootManager().getTable(this.fishingLootTable());
	}
}
