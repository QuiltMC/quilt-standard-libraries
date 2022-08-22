package org.quiltmc.qsl.entity.custom_spawn_data.test;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation.Mode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

public class BoxEntityRenderer extends EntityRenderer<BoxEntity> {
	public final BoxModel boxModel;
	public final ItemRenderer itemRenderer;

	protected BoxEntityRenderer(Context context) {
		super(context);
		this.boxModel = new BoxModel(context);
		this.itemRenderer = context.getItemRenderer();
	}

	@Override
	public void render(BoxEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		if (!entity.isInvisible()) {
			matrices.push();
			VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(getTexture(entity)));
			matrices.translate(0, -1, 0);
			boxModel.render(matrices, buffer, light, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
			ItemStack stored = entity.stored;
			if (!stored.isEmpty()) {
				matrices.push();
				matrices.scale(0.25f, 0.25f, 0.25f);
				matrices.translate(0, 7, 0);
				matrices.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(entity.age / 4f));
				itemRenderer.renderItem(stored, Mode.NONE, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, 0);
				matrices.pop();
			}
			super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
			matrices.pop();
		}
	}

	@Override
	public Identifier getTexture(BoxEntity entity) {
		return BoxModel.TEXTURE;
	}
}
