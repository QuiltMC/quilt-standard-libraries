/*
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.command.test;

import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.command.api.EntitySelectorOptionRegistry;
import org.quiltmc.qsl.command.api.QuiltEntitySelectorReader;

public class EntitySelectorOptionsTest implements ModInitializer {
	@Override
	public void onInitialize(ModContainer mod) {
		EntitySelectorOptionRegistry.register(
				new Identifier("quilt_command_testmod", "health"),
				optionReader -> {
					var reader = optionReader.getReader();
					float health = reader.readFloat();
					optionReader.setPredicate(e -> e instanceof LivingEntity l && l.getHealth() >= health);
					((QuiltEntitySelectorReader) optionReader).setFlag("selectsHealth", true);
				},
				optionReader -> !((QuiltEntitySelectorReader) optionReader).getFlag("selectsHealth"),
				Text.literal("With health greater than given value")
		);
	}
}
