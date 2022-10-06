package org.quiltmc.qsl.item.group.api.client;

import net.minecraft.client.util.math.MatrixStack;

public interface ItemGroupRenderer {
	void renderTabIcon(MatrixStack matrices, int x, int y);
}
