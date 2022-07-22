/*
 * Copyright 2022 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
