package org.quiltmc.qsl.block.extensions.test;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.block.extensions.api.QuiltMaterialBuilder;

public final class Initializer implements ModInitializer {
	public static final Material MATERIAL = QuiltMaterialBuilder.copyOf(Material.GLASS, MapColor.DARK_GREEN)
			.pistonBehavior(PistonBehavior.PUSH_ONLY)
			.build();

	public static final Block BLOCK = Registry.register(Registry.BLOCK,
			new Identifier("quilt_block_extensions_testmod", "test_block"),
			new GlassBlock(QuiltBlockSettings.copyOf(Blocks.GLASS)
					.material(MATERIAL)
					.luminance(15)));

	@Override
	public void onInitialize() {

	}
}
