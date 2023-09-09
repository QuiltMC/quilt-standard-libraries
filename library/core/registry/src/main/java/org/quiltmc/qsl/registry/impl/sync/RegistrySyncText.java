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

package org.quiltmc.qsl.registry.impl.sync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.registry.impl.sync.mod_protocol.ModProtocolDef;
import org.quiltmc.qsl.registry.impl.sync.mod_protocol.ModProtocolImpl;
import org.quiltmc.qsl.registry.impl.sync.registry.RegistryFlag;
import org.quiltmc.qsl.registry.impl.sync.registry.SynchronizedRegistry;

public class RegistrySyncText {
	public static Text missingRegistryEntries(Identifier registryId, Collection<SynchronizedRegistry.MissingEntry> missingEntries) {
		var namespacesSet = new HashSet<String>(missingEntries.size());
		for (var entry : missingEntries) {
			if (!RegistryFlag.isOptional(entry.flags())) {
				namespacesSet.add(entry.identifier().getNamespace());
			}
		}

		var namespacesList = new ArrayList<>(namespacesSet);
		namespacesList.sort(Comparator.naturalOrder());

		var namespaceText = entryList(namespacesList, Text::literal).formatted(Formatting.GRAY);

		return Text.translatableWithFallback("quilt.core.registry_sync.missing_entries", "Missing required entries in registry '%s' for namespaces:\n%s",
				Text.literal(registryId.toString()).formatted(Formatting.YELLOW),
				namespaceText
		);
	}

	private static <T> MutableText entryList(List<T> namespacesList, Function<T, Text> toText) {
		var namespaceText = Text.empty();

		var textLength = 0;
		var lines = 0;

		while (lines < 2 && !namespacesList.isEmpty()) {
			var max = lines == 0 ? 38 : 30;
			while (textLength < max && !namespacesList.isEmpty()) {
				var t = toText.apply(namespacesList.remove(0));
				namespaceText.append(t);

				textLength += t.getString().length();

				if (!namespacesList.isEmpty()) {
					var alt = (lines + toText.apply(namespacesList.get(0)).getString().length() < max && lines == 1);
					if (namespacesList.size() == 1 || alt) {
						namespaceText.append(Text.translatableWithFallback("quilt.core.registry_sync.and", " and ").formatted(alt ? Formatting.GRAY : Formatting.DARK_GRAY));
						textLength += 6;
					} else {
						namespaceText.append(Text.literal(", ").formatted(Formatting.DARK_GRAY));
						textLength += 2;
					}
				}
			}

			if (!namespacesList.isEmpty() && lines != 1) {
				namespaceText.append("\n");
			}

			textLength = 0;
			lines++;
		}

		if (!namespacesList.isEmpty()) {
			namespaceText.append(Text.translatableWithFallback("quilt.core.registry_sync.more",  "%s more...", namespacesList.size()));
		}

		return namespaceText;
	}

	public static Text mismatchedStateIds(Identifier registryId, @Nullable Identifier expectedBlockId, @Nullable Identifier foundBlockId) {
		return Text.translatableWithFallback("quilt.core.registry_sync.incorrect_state", "State validation failed.\nExpected object owner '%s' ('%s'), found '%s'",
				expectedBlockId == null ? Text.literal("null").formatted(Formatting.RED) : Text.literal(expectedBlockId.toString()).formatted(Formatting.YELLOW),
				Text.literal(registryId.toString()).formatted(Formatting.GRAY),
				foundBlockId == null ? Text.literal("null").formatted(Formatting.RED) : Text.literal(foundBlockId.toString())
		);
	}

	public static Text missingRegistry(Identifier identifier, boolean exists) {
		return Text.translatableWithFallback("quilt.core.registry_sync." + (exists ? "unsupported" : "missing") + "_registry", "Tried to sync '%s' registry, which is "  + (exists ? "unsupported" : "missing" + "!"), identifier.toString());
	}

	public static Text unsupportedModVersion(List<ModProtocolDef> unsupported, ModProtocolDef missingPrioritized) {
		if (missingPrioritized != null && !missingPrioritized.versions().isEmpty()) {
			var x = Text.translatableWithFallback("quilt.core.registry_sync.require_main_mod_protocol", "This server requires %s with protocol version of %s!",
					Text.literal(missingPrioritized.displayName()).formatted(Formatting.YELLOW),
					missingPrioritized.versions().getInt(0)
			);

			if (ModProtocolImpl.enabled && ModProtocolImpl.prioritizedEntry != null) {
				x.append("\n").append(
						Text.translatableWithFallback("quilt.core.registry_sync.main_mod_protocol", "You are on %s with protocol version of %s.",
								Text.literal(ModProtocolImpl.prioritizedEntry.displayName()).formatted(Formatting.GOLD), ModProtocolImpl.prioritizedEntry.versions().getInt(0)
						)
				);
			}

			return x;
		} else {
			System.out.println(unsupported.size());
			var namespacesList = new ArrayList<>(unsupported);
			namespacesList.sort(Comparator.comparing(ModProtocolDef::displayName));
			var namespaceText = entryList(namespacesList, RegistrySyncText::protocolDefEntryText).formatted(Formatting.GRAY);

			return Text.translatableWithFallback("quilt.core.registry_sync.unsupported_mod_protocol", "Unsupported mod protocol versions for:\n%s",
					namespaceText
			);
		}
	}

	private static Text protocolDefEntryText(ModProtocolDef def) {
		MutableText version;
		var x = def.versions();
		if (x.size() == 0) {
			version = Text.literal("WHAT??? HOW???");
		} else if (x.size() == 1) {
			version = Text.literal("" + x.getInt(0));
		} else {
			version = (MutableText) Texts.join(x.subList(0, Math.min(x.size(), 4)), n -> Text.literal(n.toString()));
			if (x.size() > 4) {
				version = version.append(Texts.GRAY_DEFAULT_SEPARATOR).append("...");
			}
		}

		return Text.translatableWithFallback("quilt.core.registry_sync.protocol_entry", "%s (%s)", def.displayName(), version);
	}
}
