/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.game_rule.impl.widget;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.EditGameRulesScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import org.quiltmc.qsl.game_rule.api.rule.DoubleRule;
import org.quiltmc.qsl.game_rule.mixin.client.EditGameRulesScreenAccessor;

@Environment(EnvType.CLIENT)
public final class DoubleRuleWidget extends EditGameRulesScreen.NamedRuleWidget {
	private final TextFieldWidget textFieldWidget;

	public DoubleRuleWidget(EditGameRulesScreen gameRuleScreen, Text name, List<OrderedText> description, final String ruleName, DoubleRule rule) {
		gameRuleScreen.super(description, name);
		EditGameRulesScreenAccessor accessor = (EditGameRulesScreenAccessor) gameRuleScreen;

		this.textFieldWidget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 10, 5, 42, 20,
				name.shallowCopy()
				.append("\n")
				.append(ruleName)
				.append("\n")
		);

		this.textFieldWidget.setText(Double.toString(rule.get()));
		this.textFieldWidget.setChangedListener(value -> {
			if (rule.validate(value)) {
				this.textFieldWidget.setEditableColor(0xE0E0E0);
				accessor.callMarkValid(this);
			} else {
				this.textFieldWidget.setEditableColor(0xFF0000);
				accessor.callMarkInvalid(this);
			}
		});

		this.children.add(this.textFieldWidget);
	}

	@Override
	public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
		// FIXME: Param names nightmare
		this.drawName(matrices, y, x);

		this.textFieldWidget.x = x + entryWidth - 44;
		this.textFieldWidget.y = y;
		this.textFieldWidget.render(matrices, mouseX, mouseY, tickDelta);
	}
}
