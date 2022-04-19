package org.quiltmc.qsl.block.extensions.impl;

import net.minecraft.block.Block;

//TODO: add a way to make any item place-able
//TODO: access widen stairs and other privated blocks by default
public class QuiltBlockImpl {
    public static final ThreadLocal<Block[]> PROXY_BLOCKS_TEMP_CONTAINER = new ThreadLocal<>();
}
