/*
 * Copyright 2023 QuiltMC
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

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class LogBuilder {
	private Text title;
	private List<Text> entriesCurrent;
	private List<Section> sections = new ArrayList<>();


	public void pushT(String id, String lang, Object... args) {
		push(Text.translatable("quilt.core.registry_sync.log." + id, lang, args));
	}
	public void push(Text title) {
		if (this.title != null) {
			sections.add(new Section(this.title, entriesCurrent));
		}

		this.title = title;
		this.entriesCurrent = new ArrayList<>();
	}

	public void textEntry(Text text) {
		text(Text.literal("- ").append(Text.empty().append(text)).formatted(Formatting.GRAY));
	}
	public void text(Text text) {
		this.entriesCurrent.add(text);
	}

	public List<Section> finish() {
		var x = this.sections;
		this.clear();
		return x;
	}

	public void clear() {
		this.sections = new ArrayList<>();
		this.title = null;
		this.entriesCurrent = null;
	}

	public String asString() {
		return stringify(this.sections);
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
