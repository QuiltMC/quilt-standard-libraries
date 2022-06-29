package org.quiltmc.qsl.component.test.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3f;
import org.quiltmc.qsl.component.test.ComponentTestMod;
import org.quiltmc.qsl.component.test.TestBlockEntity;

import java.util.function.Predicate;

public class TestBeRenderer implements BlockEntityRenderer<TestBlockEntity> {
	@Override
	public void render(TestBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		entity.expose(ComponentTestMod.CHUNK_INVENTORY)
				.map(inventoryComponent -> inventoryComponent.getStack(0))
				.filter(Predicate.not(ItemStack::isEmpty))
				.ifPresent(itemStack -> {
					matrices.push();
					matrices.translate(0.5f, 1, 0.5f);
					matrices.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(System.currentTimeMillis() % 360));
					MinecraftClient.getInstance().getItemRenderer().renderItem(
							itemStack,
							ModelTransformation.Mode.GROUND,
							0xffffff,
							OverlayTexture.DEFAULT_UV,
							matrices,
							vertexConsumers,
							0
					);
					matrices.pop();
				});
	}
}
