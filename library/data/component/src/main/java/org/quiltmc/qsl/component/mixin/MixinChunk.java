package org.quiltmc.qsl.component.mixin;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.gen.chunk.BlendingData;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.component.api.ComponentContainer;
import org.quiltmc.qsl.component.impl.LazifiedComponentContainer;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chunk.class)
public abstract class MixinChunk implements ComponentProvider {

	private ComponentContainer qsl$container;

	@Shadow
	public abstract void setNeedsSaving(boolean needsSaving);

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(ChunkPos chunkPos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<?> registry, long l, ChunkSection[] chunkSections, BlendingData blendingData, CallbackInfo ci) {
		this.qsl$container = LazifiedComponentContainer.create(this).orElseThrow();
		this.qsl$container.setSaveOperation(() -> this.setNeedsSaving(true));
	}

	@Override
	public @NotNull ComponentContainer getContainer() {
		return this.qsl$container;
	}
}
