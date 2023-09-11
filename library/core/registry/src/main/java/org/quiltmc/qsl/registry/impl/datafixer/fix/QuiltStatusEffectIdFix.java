package org.quiltmc.qsl.registry.impl.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

import net.minecraft.datafixer.TypeReferences;

// TODO - complete the rest of the owl; this hasn't even been tested
public class QuiltStatusEffectIdFix extends DataFix {
	private static final String EFFECT_ID_KEY = "quilt:effect_id";
	private static final String STATUS_EFFECT_INSTANCE_ID_KEY = "quilt:id";
	private static final String QUILT_BEACON_PRIMARY_EFFECT_KEY = "quilt:primary_effect";
	private static final String QUILT_BEACON_SECONDARY_EFFECT_KEY = "quilt:secondary_effect";
	private static final String VANILLA_BEACON_PRIMARY_EFFECT_KEY = "primary_effect";
	private static final String VANILLA_BEACON_SECONDARY_EFFECT_KEY = "secondary_effect";

	public QuiltStatusEffectIdFix(Schema schema) {
		super(schema, false);
	}

	private TypeRewriteRule blockEntityRule() {
		var type = this.getInputSchema().getType(TypeReferences.BLOCK_ENTITY);
		return this.fixTypeEverywhereTyped(
			"BlockEntityQuiltStatusEffectIdFix",
			type,
			typed -> typed.updateTyped(
				DSL.namedChoice(
					"minecraft:beacon",
					this.getInputSchema().getChoiceType(TypeReferences.BLOCK_ENTITY, "minecraft:beacon")
				),
				this.getOutputSchema().getChoiceType(TypeReferences.BLOCK_ENTITY, "minecraft:beacon"),
				typedx -> typedx.update(
					DSL.remainderFinder(),
					dynamic -> {
						var a = dynamic.get(QUILT_BEACON_PRIMARY_EFFECT_KEY).result();
                        a.ifPresent(value -> dynamic.set(VANILLA_BEACON_PRIMARY_EFFECT_KEY, new Dynamic<>((DynamicOps<String>) dynamic.getOps(), "minecraft:strength")));
						dynamic.remove(QUILT_BEACON_PRIMARY_EFFECT_KEY);

						var b = dynamic.get(QUILT_BEACON_SECONDARY_EFFECT_KEY).result();
                        b.ifPresent(value -> dynamic.set(VANILLA_BEACON_SECONDARY_EFFECT_KEY, value));
						dynamic.remove(QUILT_BEACON_SECONDARY_EFFECT_KEY);

						return dynamic;
					}
				)
			)
		);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return blockEntityRule();
	}
}
