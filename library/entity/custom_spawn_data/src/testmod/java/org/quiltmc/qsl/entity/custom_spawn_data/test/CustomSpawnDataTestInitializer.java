package org.quiltmc.qsl.entity.custom_spawn_data.test;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;

public class CustomSpawnDataTestInitializer implements ModInitializer {

	public static final EntityType<BoxEntity> BOX_TYPE = EntityType.Builder
			.<BoxEntity>create(BoxEntity::new, SpawnGroup.MISC)
			.setDimensions(0.5f, 0.5f)
			.build("box_entity");

	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(Registry.ENTITY_TYPE, new Identifier("qsl_entity_custom_spawn_data_testmod", "box_entity"), BOX_TYPE);
		CommandRegistrationCallback.EVENT.register(SpawnBoxCommand::register);
	}
}
