package org.quiltmc.qsl.worldgen.surface_rule.api.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;
import net.minecraft.world.gen.surfacebuilder.SurfaceRules;

import org.quiltmc.qsl.worldgen.surface_rule.api.SurfaceRuleContext;
import org.quiltmc.qsl.worldgen.surface_rule.api.SurfaceRuleEvents;

public record PrependMaterialRule(SurfaceRules.MaterialRule rule) implements SurfaceRuleEvents.OverworldModifierCallback,
		SurfaceRuleEvents.NetherModifierCallback,
		SurfaceRuleEvents.TheEndModifierCallback,
		SurfaceRuleEvents.GenericModifierCallback {
	public static final Codec<PrependMaterialRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			SurfaceRules.MaterialRule.CODEC.fieldOf("rule").forGetter(PrependMaterialRule::rule)
	).apply(instance, PrependMaterialRule::new));

	public static final Identifier IDENTIFIER = new Identifier("quilt", "prepend_material_rule");

	@Override
	public Identifier getCodecIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void modifyGenericSurfaceRules(@NotNull SurfaceRuleContext context) {
		context.materialRules().add(0, rule);
	}

	@Override
	public void modifyOverworldRules(SurfaceRuleContext.@NotNull Overworld context) {
		context.materialRules().add(0, rule);
	}

	@Override
	public void modifyNetherRules(SurfaceRuleContext.@NotNull Nether context) {
		context.materialRules().add(0, rule);
	}

	@Override
	public void modifyTheEndRules(SurfaceRuleContext.@NotNull TheEnd context) {
		context.materialRules().add(0, rule);
	}
}
