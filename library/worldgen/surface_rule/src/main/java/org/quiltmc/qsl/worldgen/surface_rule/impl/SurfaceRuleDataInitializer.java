package org.quiltmc.qsl.worldgen.surface_rule.impl;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.worldgen.surface_rule.api.SurfaceRuleEvents;
import org.quiltmc.qsl.worldgen.surface_rule.api.codec.AddMaterialRuleCallback;

public class SurfaceRuleDataInitializer implements ModInitializer {
	@Override
	public void onInitialize(ModContainer mod) {
		SurfaceRuleEvents.MODIFY_OVERWORLD_CODECS.register(AddMaterialRuleCallback.IDENTIFIER, AddMaterialRuleCallback.CODEC);
		SurfaceRuleEvents.MODIFY_NETHER_CODECS.register(AddMaterialRuleCallback.IDENTIFIER, AddMaterialRuleCallback.CODEC);
		SurfaceRuleEvents.MODIFY_THE_END_CODECS.register(AddMaterialRuleCallback.IDENTIFIER, AddMaterialRuleCallback.CODEC);
		SurfaceRuleEvents.MODIFY_GENERIC_CODECS.register(AddMaterialRuleCallback.IDENTIFIER, AddMaterialRuleCallback.CODEC);
	}
}
