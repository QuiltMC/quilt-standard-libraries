package org.quiltmc.qsl.block.extensions.test;

import net.minecraft.block.Block;
import org.quiltmc.qsl.block.extensions.api.QuiltBlock;
import org.quiltmc.qsl.block.extensions.impl.BlockWithProxies;

public class TestBlock extends Block implements BlockWithProxies {

    public TestBlock(Settings settings, Block... proxies) {
        super(QuiltBlock.createProxy(settings, proxies));
    }

}
