package org.quiltmc.qsl.worldgen.material_rule.api;

import net.minecraft.world.gen.surfacebuilder.SurfaceRules;

import java.util.List;

/**
 * Interface used in conjunction with the {@code qsl:material_rule_modifier} entrypoint.
 * <br><br>
 * This interface provides three default methods:
 * {@code addOverworldRules},<br>
 * {@code addNetherRules}, and <br>
 * {@code addEndRules}, <br>
 * which are used to add rules to their respective dimensions. A consumer is passed in as the sole argument. <br>
 * The consumer should be accepted with modifications
 */
public interface MaterialRuleModifier {
	default void addOverworldRules(boolean surface, boolean bedrockRoof, boolean bedrockFloor, List<SurfaceRules.MaterialRule> materialRules) {}
	default void addNetherRules(List<SurfaceRules.MaterialRule> materialRules) {}
	default void addEndRules(List<SurfaceRules.MaterialRule> materialRules) {}
}
