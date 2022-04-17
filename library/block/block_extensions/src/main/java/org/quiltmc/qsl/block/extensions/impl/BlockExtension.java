package org.quiltmc.qsl.block.extensions.impl;

import net.minecraft.block.Block;

public interface BlockExtension {
    Block getProxyOfType(Class<? extends Block> type);
    boolean isInstanceOf(Class<? extends Block> type);
    Block[] getProxies();
}
