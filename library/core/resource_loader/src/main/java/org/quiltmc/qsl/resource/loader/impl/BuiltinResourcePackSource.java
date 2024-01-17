/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.resource.loader.impl;

import net.minecraft.resource.pack.PackSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Represents a built-in resource pack source.
 * Similar to {@link PackSource#PACK_SOURCE_BUILTIN} but specifies the mod name too.
 */
public class BuiltinResourcePackSource implements PackSource {
	private static final Text SOURCE_BUILTIN_TEXT = Text.translatable("pack.source.builtin");
	private final ModNioPack pack;
	private final Text text;
	private final Text tooltip;

	BuiltinResourcePackSource(ModNioPack pack) {
		String modName = pack.modInfo.name();

		if (modName == null) {
			modName = pack.modInfo.id();
		}

		this.pack = pack;
		this.text = SOURCE_BUILTIN_TEXT;
		this.tooltip = Text.translatable("options.generic_value", SOURCE_BUILTIN_TEXT, modName);
	}

	@Override
	public Text decorate(Text description) {
		return Text.translatable("pack.nameAndSource", description, this.text).formatted(Formatting.GRAY);
	}

	@Override
	public boolean shouldAddAutomatically() {
		return this.pack.getActivationType().isEnabledByDefault();
	}

	public Text getTooltip() {
		return this.tooltip;
	}
}
