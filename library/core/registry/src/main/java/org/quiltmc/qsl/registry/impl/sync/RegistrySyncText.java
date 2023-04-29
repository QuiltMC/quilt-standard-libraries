package org.quiltmc.qsl.registry.impl.sync;

import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;

public class RegistrySyncText {
	public static Text missingRegistryEntries(Identifier registryId, Collection<SynchronizedRegistry.MissingEntry> missingEntries) {
		var namespaceText = Text.empty().formatted(Formatting.GRAY);
		var namespacesSet = new HashSet<String>(missingEntries.size());
		for (var entry : missingEntries) {
			if (!RegistryFlag.isOptional(entry.flags())) {
				namespacesSet.add(entry.identifier().getNamespace());
			}
		}
		var namespacesList = new ArrayList<>(namespacesSet);

		namespacesList.sort(Comparator.naturalOrder());

		var textLength = 0;
		var lines = 0;

		while (lines < 2 && !namespacesList.isEmpty()) {
			var max = lines == 0 ? 38 : 30;
			while (textLength < max && !namespacesList.isEmpty()) {
				var t = namespacesList.remove(0);
				namespaceText.append(t);

				textLength += t.length();

				if (!namespacesList.isEmpty()) {
					var alt = (lines + namespacesList.get(0).length() < max && lines == 1);
					if (namespacesList.size() == 1 || alt) {
						namespaceText.append(Text.translatable("quilt.core.registry_sync.and", " and ").formatted(alt ? Formatting.GRAY : Formatting.DARK_GRAY));
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
			namespaceText.append(Text.translatable("quilt.core.registry_sync.more",  "%s more...", namespacesList.size()));
		}

		return Text.translatable("quilt.core.registry_sync.missing_entries", "Missing required entries in registry '%s' for namespaces:\n%s",
				Text.literal(registryId.toString()).formatted(Formatting.YELLOW),
				namespaceText
		);
	}

	public static Text mismatchedStateIds(Identifier registryId, Identifier expectedBlockId, @Nullable Object state) {
		return Text.translatable("quilt.core.registry_sync.incorrect_state", "State validation failed.\nExpected object owner '%s' ('%s'), found '%s'",
				Text.literal(expectedBlockId.toString()).formatted(Formatting.YELLOW),
				Text.literal(registryId.toString()).formatted(Formatting.GRAY),
				state == null ? Text.literal("null").formatted(Formatting.RED) : Text.literal(state.toString())
		);	}

	public static Text missingRegistry(Identifier identifier, boolean exists) {
		return Text.translatable("quilt.core.registry_sync." + (exists ? "unsupported" : "missing") + "_registry", "Tried to sync '%s' registry, which is "  + (exists ? "unsupported" : "missing" + "!"), identifier.toString());
	}
}
