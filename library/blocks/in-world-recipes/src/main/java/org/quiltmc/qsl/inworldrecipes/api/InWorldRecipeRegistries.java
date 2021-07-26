package org.quiltmc.qsl.inworldrecipes.api;

import com.mojang.serialization.Lifecycle;
import net.minecraft.block.Block;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * <b>NOTE:</b> You need to handle action sound playback yourself.<br>
 * <b>NOTE2:</b> You <em>can</em> override vanilla recipes with this.
 */
public final class InWorldRecipeRegistries {
	private InWorldRecipeRegistries() { throw new AssertionError(); }

	public static final Registry<InWorldRecipe> SWORD = create("sword");
	public static final Registry<InWorldRecipe> PICKAXE = create("pickaxe");
	public static final Registry<InWorldRecipe> AXE = create("axe");
	public static final Registry<InWorldRecipe> SHOVEL = create("shovel");
	public static final Registry<InWorldRecipe> HOE = create("hoe");

	// TODO rewrite this method to allow for data-driven recipes
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean tryPerform(@NotNull Registry<InWorldRecipe> registry, @NotNull ItemUsageContext context, @NotNull Block targetBlock) {
		for (Map.Entry<RegistryKey<InWorldRecipe>, InWorldRecipe> entry : registry.getEntries()) {
			InWorldRecipe recipe = entry.getValue();
			if (recipe.targetBlock() != targetBlock)
				continue;
			if (recipe.tryPerform(context))
				return true;
		}
		return false;
	}

	private static @NotNull Registry<InWorldRecipe> create(@NotNull String toolName) {
		RegistryKey<Registry<InWorldRecipe>> key =
				RegistryKey.ofRegistry(new Identifier("qsl-blocks-in-world-recipes", "in_world/" + toolName));
		Registry<InWorldRecipe> registry = new SimpleRegistry<>(key, Lifecycle.stable());
		// FIXME add to root registry - not working for some reason???
		//Registry.register(Registry.REGISTRIES, key.getValue(), registry);
		return registry;
	}
}
