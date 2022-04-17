package org.quiltmc.qsl.block.extensions.test;

import net.minecraft.block.Block;
import org.quiltmc.qsl.block.extensions.api.QuiltBlock;

public class TestBlock extends Block {

    public TestBlock(Settings settings, Block... proxies) {
        super(QuiltBlock.createProxy(settings, proxies));
    }

}
