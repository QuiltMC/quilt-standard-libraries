/*
 * Copyright 2022 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.block.extensions.api;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import org.quiltmc.qsl.block.extensions.impl.BlockExtension;
import org.quiltmc.qsl.block.extensions.impl.QuiltBlockImpl;
import org.quiltmc.qsl.block.extensions.mixin.BlockMixin;

import java.util.function.Function;

public class QuiltBlock {

    /**
     * Creates a proxy from one or multiple blocks (will be temporarily stocked in a {@link ThreadLocal} and used in {@link BlockMixin}).
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
     * @param proxyState    A {@link Function} taking a Block as its input, and outputting a Blockstate, this is used to tell each proxy what state to give this method.
     * @return              A {@link BlockState} usable as a default state for our block.
     */
    public static BlockState mergeStates(Block ownerBlock, Function<Block ,BlockState> proxyState) {
        var proxies = ((BlockExtension) ownerBlock).getProxies();
        var ownerState = ownerBlock.getDefaultState();
        for (var proxy : proxies) {
            var appliedProxyState = proxyState.apply(proxy);
            for (Property<?> property : appliedProxyState.getProperties()) {
                ownerState = QuiltBlock.copyProperty(appliedProxyState, ownerState, property);
            }
        }
        return ownerState;
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
        if (target.contains(property)) {
            return target.with(property, source.get(property));
        }
        else {
            QuiltBlockImpl.LOGGER.warn("Failed to copy properties from {}, {} does not exist in {}", source, property, target);
            return target;
        }
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
            QuiltBlockImpl.LOGGER.warn("Cannot get property {} as it does not exist in {}, falling back on null", property, state.getBlock());
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
