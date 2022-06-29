package org.quiltmc.qsl.component.test.mixin.client;

import net.minecraft.client.ClientGameSession;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import org.quiltmc.qsl.component.api.components.IntegerComponent;
import org.quiltmc.qsl.component.test.ComponentTestMod;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {

	@Shadow
	public abstract TextRenderer getTextRenderer();

	@Shadow
	@Final
	private ItemRenderer itemRenderer;

	@Inject(method = "render", at = @At("TAIL"))
	private void renderCustom(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
		Entity entity = MinecraftClient.getInstance().targetedEntity;
		if (entity != null) {
			entity.expose(ComponentTestMod.HOSTILE_EXPLODE_TIME).map(IntegerComponent::get).ifPresent(integer -> {
				this.getTextRenderer().draw(matrices, integer.toString(), 10, 10, 0xfafafa);
			});
		}
		ChunkPos chunkPos = MinecraftClient.getInstance().player.getChunkPos();
		MinecraftClient.getInstance().world.getChunk(chunkPos.x, chunkPos.z).expose(ComponentTestMod.CHUNK_INVENTORY)
				.map(defaultInventoryComponent -> defaultInventoryComponent.getStack(0))
				.ifPresent(itemStack -> this.itemRenderer.renderInGui(itemStack, 10, 10));
	}
}
