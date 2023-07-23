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

package org.quiltmc.qsl.registry.impl.sync.client;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import org.quiltmc.loader.api.minecraft.ClientOnly;

@ApiStatus.Internal
@ClientOnly
public class LogBuilder {
	private Text title;
	private List<Text> entriesCurrent;
	private List<Section> sections = new ArrayList<>();
	private Text currentText = null;
	private int duplicateCount = 0;

	public void pushT(String id, String lang, Object... args) {
		this.push(Text.translatableWithFallback("quilt.core.registry_sync.log." + id, lang, args));
	}

	public void push(Text title) {
		if (this.title != null) {
			this.sections.add(new Section(this.title, this.entriesCurrent));
		}

		this.title = title;
		this.entriesCurrent = new ArrayList<>();
	}

	public void textEntry(Text text) {
		this.text(Text.literal("- ").append(Text.empty().append(text)).formatted(Formatting.GRAY));
	}

	public void text(Text text) {
		if (this.currentText != null && !text.equals(this.currentText)) {
			this.entriesCurrent.add(duplicatedText(this.currentText, this.duplicateCount));
			this.duplicateCount = 1;
		} else {
			this.duplicateCount++;
		}

		this.currentText = text;
	}

	public List<Section> finish() {
		if (this.title != null) {
			var y = new ArrayList<>(this.entriesCurrent);
			if (this.currentText != null) {
				y.add(duplicatedText(this.currentText, this.duplicateCount));
			}

			this.sections.add(new Section(this.title, y));
		}

		var x = this.sections;
		this.clear();
		return x;
	}

	private static Text duplicatedText(Text currentText, int duplicateCount) {
		if (duplicateCount < 2) {
			return currentText;
		} else {
			return Text.empty().append(currentText).append(Text.literal(" (" + duplicateCount + ")").formatted(Formatting.BLUE));
		}
	}

	public void clear() {
		this.sections = new ArrayList<>();
		this.title = null;
		this.entriesCurrent = null;
	}

	public String asString() {
		var sections = new ArrayList<>(this.sections);
		if (this.title != null) {
			sections.add(new Section(this.title, this.entriesCurrent));
		}

		return stringify(sections);
	}

	public record Section(Text title, List<Text> entries) {};

	public static String stringify(List<Section> sections) {
		var builder = new StringBuilder();
		var iter = sections.iterator();

		while (iter.hasNext()) {
			var entry = iter.next();
			builder.append("## " + entry.title().getString());

			if (entry.entries.size() > 0) {
				builder.append("\n");
				var eIter = entry.entries.iterator();
				while (eIter.hasNext()) {
					builder.append("   " + eIter.next().getString());
					if (eIter.hasNext()) {
						builder.append("\n");
					}
				}
			}

			if (iter.hasNext()) {
				builder.append("\n");
			}
		}

		return builder.toString();
	}
}
