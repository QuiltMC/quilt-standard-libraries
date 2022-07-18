package org.quiltmc.qsl.worldgen.material_rule.impl;

import net.minecraft.world.gen.surfacebuilder.SurfaceRules;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Used to hold material rule sequences for injection for the {@code MaterialRuleRegistrationEvents}.</p>
 * <p>Do <b>not</b> modify or in any other way touch these, they are public for mixin access.</p>
 * <p>Tampering with this is likely to result in mod conflict, and to largely alter the vanilla surface.</p>
 * @see org.quiltmc.qsl.worldgen.material_rule.api.MaterialRuleRegistrationEvents
 */
@Deprecated
public class RuleHolder {
	public static final List<SurfaceRules.MaterialRule> overworldRules = new ArrayList<>();
	public static final List<SurfaceRules.MaterialRule> netherRules = new ArrayList<>();
	public static final List<SurfaceRules.MaterialRule> theEndRules = new ArrayList<>();
}
