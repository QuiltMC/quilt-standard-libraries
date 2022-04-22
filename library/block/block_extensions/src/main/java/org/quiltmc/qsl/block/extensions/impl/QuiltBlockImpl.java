package org.quiltmc.qsl.block.extensions.impl;

import net.minecraft.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO: add a way to make any item place-able
//TODO: access widen stairs and other privated blocks by default
public class QuiltBlockImpl {
    public static final Logger LOGGER = LoggerFactory.getLogger("Quilt Block Impl");
    public static final ThreadLocal<Block[]> PROXY_BLOCKS_TEMP_CONTAINER = new ThreadLocal<>();
}
