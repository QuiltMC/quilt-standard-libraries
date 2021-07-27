package org.quiltmc.qsl.toolinteractionrecipes.api;

import com.mojang.serialization.Lifecycle;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * <b>NOTE:</b> You need to handle action sound playback yourself.<br>
 * <b>NOTE2:</b> You <em>can</em> override vanilla recipes with this.
 */
public final class ToolInteractionRecipeRegistries {
	private ToolInteractionRecipeRegistries() { throw new AssertionError(); }

	public static final Registry<ToolInteractionRecipe> SWORD = create("sword");
	public static final Registry<ToolInteractionRecipe> PICKAXE = create("pickaxe");
	public static final Registry<ToolInteractionRecipe> AXE = create("axe");
	public static final Registry<ToolInteractionRecipe> SHOVEL = create("shovel");
	public static final Registry<ToolInteractionRecipe> HOE = create("hoe");

	private static @NotNull Registry<ToolInteractionRecipe> create(@NotNull String toolName) {
		RegistryKey<Registry<ToolInteractionRecipe>> key =
				RegistryKey.ofRegistry(new Identifier("qsl-blocks-tool-interaction-recipes", "tool_interaction/" + toolName));
		Registry<ToolInteractionRecipe> registry = new SimpleRegistry<>(key, Lifecycle.stable());
		// FIXME add to root registry (when Quilt has a registry library)
		//Registry.register(Registry.REGISTRIES, key.getValue(), registry);
		return registry;
	}
}
