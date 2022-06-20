package org.quiltmc.qsl.component.mixin;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.gen.chunk.BlendingData;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.components.NbtComponent;
import org.quiltmc.qsl.component.api.identifier.ComponentIdentifier;
import org.quiltmc.qsl.component.impl.util.duck.NbtComponentProvider;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;

@Implements({
		@Interface(iface = ComponentProvider.class, prefix = "comp$"),
		@Interface(iface = NbtComponentProvider.class, prefix = "nbtExp$")
})
@Mixin(Chunk.class)
public abstract class MixinChunk {

	@Shadow
	public abstract void setNeedsSaving(boolean needsSaving);

	private Map<Identifier, Component> qsl$components;
	private Map<Identifier, NbtComponent<?>> qsl$nbtComponents;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(ChunkPos chunkPos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<?> registry, long l, ChunkSection[] chunkSections, BlendingData blendingData, CallbackInfo ci) {
		this.qsl$components = ComponentProvider.createComponents((ComponentProvider) this);
		this.qsl$nbtComponents = NbtComponent.getNbtSerializable(this.qsl$components);
		this.qsl$nbtComponents.forEach((ignored, nbtComponent) -> nbtComponent.setSaveOperation(() -> this.setNeedsSaving(true)));
	}

	public Map<Identifier, NbtComponent<?>> nbtExp$getNbtComponents() {
		return this.qsl$nbtComponents;
	}

	public Optional<Component> comp$expose(ComponentIdentifier<?> id) {
		return Optional.ofNullable(this.qsl$components.get(id.id()));
	}

	public Map<Identifier, Component> comp$exposeAll() {
		return this.qsl$components;
	}
}
