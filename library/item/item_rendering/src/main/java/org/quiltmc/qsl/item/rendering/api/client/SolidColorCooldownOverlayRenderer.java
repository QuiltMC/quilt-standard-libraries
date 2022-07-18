package org.quiltmc.qsl.item.rendering.api.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tessellator;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public abstract class SolidColorCooldownOverlayRenderer implements CooldownOverlayRenderer {
	@Override
	public void renderCooldownOverlay(MatrixStack matrices, TextRenderer renderer, float zOffset, ItemStack stack) {
		int step = getCooldownOverlayStep(stack);
		int color = getCooldownOverlayColor(stack);

		RenderSystem.disableDepthTest();
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBufferBuilder();
		this.renderGuiQuad(matrices, buffer, 0, 16 - step, 16, step, color);
		RenderSystem.enableTexture();
		RenderSystem.enableDepthTest();
	}

	protected abstract boolean isCooldownOverlayVisible(ItemStack stack);

	/**
	 * {@return the height of the cooldown overlay in pixels (out of 13)}
	 */
	protected abstract int getCooldownOverlayStep(ItemStack stack);

	protected abstract int getCooldownOverlayColor(ItemStack stack);
}
