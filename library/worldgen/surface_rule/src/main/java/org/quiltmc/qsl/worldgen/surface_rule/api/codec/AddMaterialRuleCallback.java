package org.quiltmc.qsl.worldgen.surface_rule.api.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;
import net.minecraft.world.gen.surfacebuilder.SurfaceRules;

import org.quiltmc.qsl.worldgen.surface_rule.api.SurfaceRuleContext;
import org.quiltmc.qsl.worldgen.surface_rule.api.SurfaceRuleEvents;

/**
 * A surface rule callback that adds a material rule to a surface rule's list of material rules.
 * @param append if true, the rule will be added to the end of the list of material rules. If false, the rule will be added to the beginning.
 */
public record AddMaterialRuleCallback(SurfaceRules.MaterialRule rule, boolean append) implements SurfaceRuleEvents.OverworldModifierCallback,
		SurfaceRuleEvents.NetherModifierCallback,
		SurfaceRuleEvents.TheEndModifierCallback,
		SurfaceRuleEvents.GenericModifierCallback {
	public static final Codec<AddMaterialRuleCallback> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			SurfaceRules.MaterialRule.CODEC.fieldOf("rule").forGetter(AddMaterialRuleCallback::rule),
			Codec.BOOL.optionalFieldOf("append", false).forGetter(AddMaterialRuleCallback::append)
	).apply(instance, AddMaterialRuleCallback::new));

	public static final Identifier IDENTIFIER = new Identifier("quilt", "add_material_rule");

	@Override
	public Identifier getCodecIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void modifyGenericSurfaceRules(@NotNull SurfaceRuleContext context) {
		this.modifyRules(context);
	}

	@Override
	public void modifyOverworldRules(SurfaceRuleContext.@NotNull Overworld context) {
		this.modifyRules(context);
	}

	@Override
	public void modifyNetherRules(SurfaceRuleContext.@NotNull Nether context) {
		this.modifyRules(context);
	}

	@Override
	public void modifyTheEndRules(SurfaceRuleContext.@NotNull TheEnd context) {
		this.modifyRules(context);
	}

	private void modifyRules(SurfaceRuleContext context) {
		if (append)
			context.materialRules().add(rule);
		else
			context.materialRules().add(0, rule);
	}
}
