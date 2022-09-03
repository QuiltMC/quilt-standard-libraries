package org.quiltmc.qsl.command.api;

import net.minecraft.command.EntitySelectorOptions;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.command.mixin.EntitySelectorOptionsAccessor;

import java.util.function.Predicate;

/**
 * Class to allow registration of custom {@link net.minecraft.command.EntitySelectorOptions entity selector options}.
 *
 * <p>These are registered with namespaced IDs to avoid name collisions. These are then converted to a name of the form
 * "namespace.path". Due to {@link com.mojang.brigadier.StringReader#isAllowedInUnquotedString(char) limitations} in how
 * entity selectors may be named, the character "{@code /}" cannot be included in entity selector names.</p>
 */
public final class EntitySelectorOptionRegistry {
	/**
	 * Registers an entity selector option. It is recommended to call this in your mod initializer.
	 *
	 * @param id			the ID of the option. This is used as the option name in the form "namespace+path"
	 * @param handler		the handler for the option
	 * @param condition		the condition under which the option is available
	 * @param description	a description of the option
	 *
	 * @throws IllegalArgumentException if the ID contains an illegal character
	 */
	public static void register(Identifier id, EntitySelectorOptions.SelectorHandler handler, Predicate<EntitySelectorReader> condition, Text description) {
		if (id.toString().contains("/")) {
			throw new IllegalArgumentException("Entity Selector Option %s has illegal character %s in ID".formatted(id, "/"));
		}

		EntitySelectorOptionsAccessor.callPutOption(id.getNamespace() + "." + id.getPath(), handler, condition, description);
	}
}
