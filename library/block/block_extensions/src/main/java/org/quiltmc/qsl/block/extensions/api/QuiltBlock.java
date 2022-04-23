package org.quiltmc.qsl.block.extensions.api;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import org.quiltmc.qsl.block.extensions.impl.BlockExtension;
import org.quiltmc.qsl.block.extensions.impl.QuiltBlockImpl;

public class QuiltBlock {

    /**
     * Creates a proxy from one or multiple blocks (will be temporarily stocked in a {@link ThreadLocal} and used in {@link org.quiltmc.qsl.block.extensions.mixin.MixinBlock}).
     * @param settings  Block settings of the block you want to add Proxies for (dummy usage, to be able to insert logic at {@code  super()} call.
     * @param proxies   Array of proxy blocks to be "merged" together as one (property merging).
     * @return          Same Block settings as above.
     */
    public static <S extends AbstractBlock.Settings> S createProxy(S settings, Block... proxies) {
        QuiltBlockImpl.PROXY_BLOCKS_TEMP_CONTAINER.set(proxies);
        return settings;
    }

    /**
     * Creates a default state {@link BlockState} for blocks that have Proxies.
     * @param ownerBlock    The block that "owns" the proxies ot create the state from.
     * @return              A {@link BlockState} usable as a default state for our block.
     */
    public static BlockState getProxyDefaultState(Block ownerBlock) {
        var proxies = ((BlockExtension) ownerBlock).getProxies();
        var defaultState = ownerBlock.getDefaultState();
        for (var proxy : proxies) {
            var proxyDefaultState = proxy.getDefaultState();
            for (Property<?> property : proxy.getDefaultState().getProperties()) {
                defaultState = QuiltBlock.copyProperty(proxyDefaultState, defaultState, property);
            }
        }
        return defaultState;
    }

    /**
     * Copies a {@code property} from a {@code source} to a {@code target} {@link BlockState}.
     * Shamelessly copied from {@link Block} because it just works.
     * @param source    The source {@link BlockState}
     * @param target    The target {@link BlockState}
     * @param property  The {@link Property} to copy from {@code source} to {@code target}
     * @return          A {@link BlockState} where all properties have been transferred from {@code source} to {@code target}
     * @param <T>       The {@link Property}'s type
     */
    public static <T extends Comparable<T>> BlockState copyProperty(BlockState source, BlockState target, Property<T> property) {
        return target.with(property, source.get(property));
    }

    /**
     * Safely get a property's value from our {@code Block}'s current {@code state}
     * @param state     The state we want to get the value from.
     * @param property  The property we're looking for.
     * @return          The value of the targeted property, OR a typed null if the peroperty doesn't exist in this block.
     * @param <T>       The type of the target property.
     */
    public static <T extends Comparable<T>> T getProperty(BlockState state, Property<T> property) {
        if (state.contains(property)) {
            return state.get(property);
        } else {
            QuiltBlockImpl.LOGGER.warn(String.format("Cannot get property %s as it does not exist in %s, falling back on null", property, state.getBlock()));
            return (T) null;
        }
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
