package org.quiltmc.qsl.crash.test.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndCrystalEntity.class)
public abstract class EndCrystalEntityMixin extends Entity {
	public EndCrystalEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject(method = "tick", at=@At("HEAD"))
	void crashOnTick(CallbackInfo ci) {
		if (world.getBlockState(getBlockPos().down()).getBlock() == Blocks.DIAMOND_BLOCK) {
			kill();
			throw new RuntimeException("Crash Test!");
		}
	}
}
