package org.quiltmc.qsl.rendering.item.impl.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.rendering.item.api.client.QuadHelper;
import org.quiltmc.qsl.rendering.item.mixin.client.BufferBuilderAccessor;

@Environment(EnvType.CLIENT)
public final class QuadHelperImpl implements QuadHelper {
	private static final Logger LOGGER = LogUtils.getLogger();

	public enum State {
		NONE, COLORED, TEXTURED
	}

	private final BufferBuilder bufferBuilder;
	private State state;
	private @Nullable Identifier texture;

	public QuadHelperImpl() {
		this.bufferBuilder = Tessellator.getInstance().getBufferBuilder();
		this.state = State.NONE;
		this.texture = null;
	}

	private void initState(State targetState) {
		if (state == targetState) {
			return;
		}

		if (bufferBuilder.isBuilding()) {
			LOGGER.warn("Resetting BufferBuilder in the middle of building! This shouldn't happen");
			((BufferBuilderAccessor) bufferBuilder).callReset();
		}

		switch (targetState) {
		case COLORED -> {
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		}
		case TEXTURED -> {
			if (texture == null) {
				throw new IllegalStateException("tried to set TEXTURED state with no texture");
			}
			RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
			RenderSystem.setShaderTexture(0, texture);
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		}
		}

		state = targetState;
	}

	@Override
	public @NotNull BufferBuilder getBufferBuilder() {
		flush();
		initState(State.NONE); // untrack current state, so we can properly reset it
		return bufferBuilder;
	}

	@Override
	public void reinitialize() {
		if (texture == null) {
			initState(State.TEXTURED);
		} else {
			initState(State.COLORED);
		}
	}

	@Override
	public void flush() {
		if (bufferBuilder.isBuilding()) {
			var buffer = bufferBuilder.endOrDiscard();
			if (buffer != null) {
				BufferRenderer.drawWithShader(buffer);
			}
		}
	}

	@Override
	public @NotNull BufferBuilder beginQuad() {
		if (texture != null) {
			flush();
			texture = null;
		}
		reinitialize();
		return bufferBuilder;
	}

	@Override
	public @NotNull BufferBuilder beginTexturedQuad(@NotNull Identifier texture) {
		if (!texture.equals(this.texture)) {
			flush();
			this.texture = texture;
		}
		reinitialize();
		return bufferBuilder;
	}
}
