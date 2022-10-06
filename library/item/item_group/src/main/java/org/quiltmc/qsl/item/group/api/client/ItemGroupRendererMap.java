package org.quiltmc.qsl.item.group.api.client;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemGroup;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Environment(EnvType.CLIENT)
public final class ItemGroupRendererMap {
	private ItemGroupRendererMap() {
	}

	private static final Map<ItemGroup, ItemGroupRenderer> rendererMap = new Reference2ReferenceOpenHashMap<>();

	public static void put(ItemGroup group, ItemGroupRenderer renderer) {
		rendererMap.put(group, renderer);
	}

	@Nullable
	public static ItemGroupRenderer get(ItemGroup group) {
		return rendererMap.get(group);
	}
}
