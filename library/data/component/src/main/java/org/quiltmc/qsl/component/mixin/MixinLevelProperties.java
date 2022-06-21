package org.quiltmc.qsl.component.mixin;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.SaveVersionInfo;
import net.minecraft.world.timer.Timer;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.components.NbtComponent;
import org.quiltmc.qsl.component.api.identifier.ComponentIdentifier;
import org.quiltmc.qsl.component.impl.util.StringConstants;
import org.quiltmc.qsl.component.impl.util.duck.NbtComponentProvider;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Implements({
		@Interface(iface = ComponentProvider.class, prefix = "comp$"),
		@Interface(iface = NbtComponentProvider.class, prefix = "nbtExp$")
})
@Mixin(LevelProperties.class)
public abstract class MixinLevelProperties implements ServerWorldProperties, SaveProperties {
	private Map<Identifier, Component> qsl$components;
	private Map<Identifier, NbtComponent<?>> qsl$nbtComponents;

	@Inject(method = "readProperties", at = @At("RETURN"))
	private static void readComponentData(Dynamic<NbtElement> dynamic, DataFixer dataFixer, int dataVersion, @Nullable NbtCompound playerData, LevelInfo levelInfo, SaveVersionInfo saveVersionInfo, GeneratorOptions generatorOptions, Lifecycle lifecycle, CallbackInfoReturnable<LevelProperties> cir) {
		LevelProperties retProperties = cir.getReturnValue();

		dynamic.get(StringConstants.COMPONENT_ROOT)
				.result()
				.map(Dynamic::getValue)
				.map(it -> (NbtCompound) it) // Casting here since we know it *cannot* be anything *but* NbtCompound.
				.ifPresent(rootQslNbt -> ((NbtComponentProvider) retProperties).getNbtComponents().forEach(
						(identifier, nbtComponent) -> NbtComponent.readFrom(nbtComponent, identifier, rootQslNbt)
				));
	}

	@Inject(method = "updateProperties", at = @At("TAIL"))
	private void writeComponentData(DynamicRegistryManager registryManager, NbtCompound levelNbt, NbtCompound playerNbt, CallbackInfo ci) {
		var rootQslNbt = new NbtCompound();
		this.qsl$nbtComponents.forEach((identifier, nbtComponent) -> rootQslNbt.put(identifier.toString(), nbtComponent.write()));
		levelNbt.put(StringConstants.COMPONENT_ROOT, rootQslNbt);
	}

	@Inject(method = "<init>(Lcom/mojang/datafixers/DataFixer;ILnet/minecraft/nbt/NbtCompound;ZIIIFJJIIIZIZZZLnet/minecraft/world/border/WorldBorder$Properties;IILjava/util/UUID;Ljava/util/Set;Lnet/minecraft/world/timer/Timer;Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/world/level/LevelInfo;Lnet/minecraft/world/gen/GeneratorOptions;Lcom/mojang/serialization/Lifecycle;)V", at = @At("TAIL"))
	private void onInit(DataFixer dataFixer, int i, NbtCompound nbtCompound, boolean bl, int j, int k, int l, float f, long m, long n, int o, int p, int q, boolean bl2, int r, boolean bl3, boolean bl4, boolean bl5, WorldBorder.Properties properties, int s, int t, UUID uUID, Set<String> set, Timer<MinecraftServer> timer, NbtCompound nbtCompound2, NbtCompound nbtCompound3, LevelInfo levelInfo, GeneratorOptions generatorOptions, Lifecycle lifecycle, CallbackInfo ci) {
		this.qsl$components = ComponentProvider.createComponents((ComponentProvider) this);
		this.qsl$nbtComponents = NbtComponent.getNbtSerializable(this.qsl$components);
	}

	public Optional<Component> comp$expose(ComponentIdentifier<?> id) {
		return Optional.ofNullable(this.qsl$components.get(id.id()));
	}

	public Map<Identifier, Component> comp$exposeAll() {
		return this.qsl$components;
	}

	public Map<Identifier, NbtComponent<?>> nbtExp$getNbtComponents() {
		return this.qsl$nbtComponents;
	}
}
