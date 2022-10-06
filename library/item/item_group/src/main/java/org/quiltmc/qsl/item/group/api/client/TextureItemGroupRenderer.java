package org.quiltmc.qsl.item.group.api.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class TextureItemGroupRenderer implements ItemGroupRenderer {

	public final Identifier location;

	public TextureItemGroupRenderer(Identifier location) {
		this.location = location;
	}

	@Override
	public void renderTabIcon(MatrixStack matrices, int x, int y) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, location);
		DrawableHelper.drawTexture(matrices, x, y, 100, 0f, 0f, 16, 16, 16, 16);
	}
}
