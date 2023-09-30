/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.screen.impl.client;

import java.util.AbstractList;
import java.util.List;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ClickableWidget;

import org.quiltmc.loader.api.minecraft.ClientOnly;

// TODO: When events for listening to addition of child elements are added, fire events from this list.
@ApiStatus.Internal
@ClientOnly
public final class ButtonList extends AbstractList<ClickableWidget> {
	private final List<Drawable> drawables;
	private final List<Selectable> selectables;
	private final List<Element> children;

	public ButtonList(List<Drawable> drawables, List<Selectable> selectables, List<Element> children) {
		this.drawables = drawables;
		this.selectables = selectables;
		this.children = children;
	}

	@Override
	public ClickableWidget get(int index) {
		final int drawableIndex = this.translateIndex(this.drawables, index, false);
		return (ClickableWidget) this.drawables.get(drawableIndex);
	}

	@Override
	public ClickableWidget set(int index, ClickableWidget element) {
		final int drawableIndex = this.translateIndex(this.drawables, index, false);
		this.drawables.set(drawableIndex, element);

		final int selectableIndex = this.translateIndex(this.selectables, index, false);
		this.selectables.set(selectableIndex, element);

		final int childIndex = this.translateIndex(this.children, index, false);
		return (ClickableWidget) this.children.set(childIndex, element);
	}

	@Override
	public void add(int index, ClickableWidget element) {
		// ensure no duplicates
		final int duplicateIndex = this.drawables.indexOf(element);

		if (duplicateIndex >= 0) {
			this.drawables.remove(element);
			this.selectables.remove(element);
			this.children.remove(element);

			if (duplicateIndex <= this.translateIndex(this.drawables, index, true)) {
				index--;
			}
		}

		final int drawableIndex = this.translateIndex(this.drawables, index, true);
		this.drawables.add(drawableIndex, element);

		final int selectableIndex = this.translateIndex(this.selectables, index, true);
		this.selectables.add(selectableIndex, element);

		final int childIndex = this.translateIndex(this.children, index, true);
		this.children.add(childIndex, element);
	}

	@Override
	public ClickableWidget remove(int index) {
		index = this.translateIndex(this.drawables, index, false);

		final var removedButton = (ClickableWidget) this.drawables.remove(index);
		this.selectables.remove(removedButton);
		this.children.remove(removedButton);

		return removedButton;
	}

	@Override
	public int size() {
		int ret = 0;

		for (var drawable : this.drawables) {
			if (drawable instanceof ClickableWidget) {
				ret++;
			}
		}

		return ret;
	}

	private int translateIndex(List<?> list, int index, boolean allowAfter) {
		int remaining = index;

		for (int i = 0, max = list.size(); i < max; i++) {
			if (list.get(i) instanceof ClickableWidget) {
				if (remaining == 0) {
					return i;
				}

				remaining--;
			}
		}

		if (allowAfter && remaining == 0) {
			return list.size();
		}

		throw new IndexOutOfBoundsException(String.format("Index: %d, Size: %d", index, index - remaining));
	}
}
