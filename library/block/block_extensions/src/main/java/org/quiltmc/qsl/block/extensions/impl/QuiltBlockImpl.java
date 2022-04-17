package org.quiltmc.qsl.block.extensions.impl;

import net.minecraft.block.Block;

public class QuiltBlockImpl {
    public static final ThreadLocal<Block[]> PROXY_BLOCKS_TEMP_CONTAINER = new ThreadLocal<>();
}
