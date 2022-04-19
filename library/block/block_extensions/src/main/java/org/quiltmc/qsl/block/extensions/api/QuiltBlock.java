package org.quiltmc.qsl.block.extensions.api;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import org.quiltmc.qsl.block.extensions.impl.BlockExtension;
import org.quiltmc.qsl.block.extensions.impl.QuiltBlockImpl;

//TODO: find a way to proxy default states
public class QuiltBlock {

    /**
     * Creates a proxy from one or multiple blocks (will be temporarily stocked in a {@link ThreadLocal} and used in {@link org.quiltmc.qsl.block.extensions.mixin.MixinBlock}).
     * @param settings  Block settings of the block you want to add Proxies for (dummy usage, to be able to insert logic at {@code  super()} call.
     * @param proxies   Array of proxy blocks to be "merged" together as one (property merging).
     * @return          Same Block settings as above.
     */
    public static AbstractBlock.Settings createProxy(AbstractBlock.Settings settings, Block... proxies) {
        QuiltBlockImpl.PROXY_BLOCKS_TEMP_CONTAINER.set(proxies);
        return settings;
    }

    //Doesn't work at all, I hate my life
    /**
     * Creates a default state {@link BlockState} for blocks that have Proxies.
     * @param ownerBlock    The block that "owns" the proxies ot create the state from.
     * @return              A {@link BlockState} usable as a default state for our block.
     */
    public static BlockState getProxyDefaultState(Block ownerBlock) {
        var proxies = ((BlockExtension) ownerBlock).getProxies();
        var defaultState = ownerBlock.getDefaultState();
        for (var proxy : proxies) {
            for (var property : proxy.getDefaultState().getProperties()) {
                var a = (Property) property;
                defaultState.with(a, proxy.getDefaultState().get(property));
            }
        }
        return defaultState;
    }

    /**
     * Checks if this block has been built using {@code blockClass} as a Proxy
     * @param blockClass    The Block class to check this block against
     * @return              boolean telling us weither this block has {@code blockClass} as a proxy or not
     */
    public boolean isInstanceOfProxy(Class<? extends Block> blockClass) {
        return ((BlockExtension) this).isInstanceOf(blockClass);
    }
}
