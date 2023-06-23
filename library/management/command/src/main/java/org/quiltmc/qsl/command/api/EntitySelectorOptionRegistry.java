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

package org.quiltmc.qsl.command.api;

import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import net.minecraft.command.EntitySelectorOptions;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.command.mixin.EntitySelectorOptionsAccessor;

/**
 * Class to allow registration of custom {@link net.minecraft.command.EntitySelectorOptions entity selector options}.
 * <p>
 * These are registered with namespaced identifiers to avoid name collisions. These are then converted to a name of the form
 * "namespace_path". Due to {@linkplain com.mojang.brigadier.StringReader#isAllowedInUnquotedString(char) limitations} in how
 * entity selectors may be named, the character "{@code /}" is replaced with "{@code _}".
 */
public final class EntitySelectorOptionRegistry {
	/**
	 * Registers an entity selector option. It is recommended to call this in your mod initializer.
	 *
	 * @param id			the identifier of the option. This is turned into an underscore-separated string for the option's name
	 * @param handler		the handler for the option
	 * @param condition		the condition under which the option is available
	 * @param description	a description of the option
	 */
	public static void register(@NotNull Identifier id, @NotNull EntitySelectorOptions.SelectorHandler handler,
			@NotNull Predicate<EntitySelectorReader> condition, @NotNull Text description) {
		EntitySelectorOptionsAccessor.callPutOption(id.toUnderscoreSeparatedString(), handler, condition, description);
	}
}
