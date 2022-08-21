package org.quiltmc.qsl.tracked_data.test;

import net.fabricmc.api.EnvType;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.tracked_data.api.QuiltTrackedDataHandlerRegistry;

public class TrackedDataTestInitializer implements ModInitializer {
	public static final TrackedDataHandler<ParticleEffect> PARTICLE_DATA_HANDLER = new TrackedDataHandler.SimpleHandler<>() {
		@Override
		public void write(PacketByteBuf buf, ParticleEffect value) {
			buf.writeId(Registry.PARTICLE_TYPE, value.getType());
			value.write(buf);
		}

		@Override
		public ParticleEffect read(PacketByteBuf buf) {
			ParticleType type = buf.readById(Registry.PARTICLE_TYPE);
			return type.getParametersFactory().read(type, buf);
		}
	};

	public static final TrackedDataHandler<StatusEffect> TEST_HANDLER = TrackedDataHandler.createIndexed(Registry.STATUS_EFFECT);

	public static final TrackedDataHandler<StatusEffect> BAD_EXAMPLE_HANDLER = TrackedDataHandler.createIndexed(Registry.STATUS_EFFECT);


	@Override
	public void onInitialize(ModContainer mod) {
		if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT) {
			QuiltTrackedDataHandlerRegistry.register(new Identifier("quilt_test_mod", "particle"), PARTICLE_DATA_HANDLER);
			QuiltTrackedDataHandlerRegistry.register(new Identifier("quilt_test_mod", "test"), TEST_HANDLER);
		} else {
			QuiltTrackedDataHandlerRegistry.register(new Identifier("quilt_test_mod", "test"), TEST_HANDLER);
			QuiltTrackedDataHandlerRegistry.register(new Identifier("quilt_test_mod", "particle"), PARTICLE_DATA_HANDLER);
		}

		// Dont do that
		TrackedDataHandlerRegistry.register(BAD_EXAMPLE_HANDLER);
	}
}
