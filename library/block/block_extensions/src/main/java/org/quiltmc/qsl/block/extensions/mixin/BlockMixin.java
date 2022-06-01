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

package org.quiltmc.qsl.block.extensions.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import org.apache.commons.compress.utils.Lists;
import org.quiltmc.qsl.block.extensions.impl.BlockExtension;
import org.quiltmc.qsl.block.extensions.impl.QuiltBlockImpl;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.LinkedList;

@Mixin(Block.class)
public abstract class BlockMixin implements BlockExtension {

    @Shadow protected abstract Block asBlock();
    @Shadow @Final private static Logger LOGGER;
    private final @Unique LinkedList<Block> proxies = new LinkedList<>();

    @Inject(method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/state/StateManager$Builder;build(Ljava/util/function/Function;Lnet/minecraft/state/StateManager$Factory;)Lnet/minecraft/state/StateManager;"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectProxyProperties(AbstractBlock.Settings settings, CallbackInfo ci, StateManager.Builder<Block, BlockState> builder) {
        if (QuiltBlockImpl.PROXY_BLOCKS_TEMP_CONTAINER.get() != null && QuiltBlockImpl.PROXY_BLOCKS_TEMP_CONTAINER.get().length > 0){
            var properties = new ArrayList<Property>();
            for (var proxyBlock : QuiltBlockImpl.PROXY_BLOCKS_TEMP_CONTAINER.get()) {
                for (var property : proxyBlock.getStateManager().getProperties()) {
                    if (!properties.contains(property)) {
                        properties.add(property);
                    }
                }
                this.proxies.add(proxyBlock);
            }
            builder.add(properties.toArray(Property[]::new));
            QuiltBlockImpl.PROXY_BLOCKS_TEMP_CONTAINER.remove();
        }
    }

    @Override
    public boolean isInstanceOf(Class<? extends Block> type) {
        if (!this.proxies.isEmpty()){
            for (var proxy : this.proxies) {
                if (proxy.getClass().isAssignableFrom(type)) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Block getProxyOfType(Class<? extends Block> type) {
        var result = (Block) null;
        if (!this.proxies.isEmpty()){
            for (var proxy : this.proxies) {
                if (proxy.getClass().isAssignableFrom(type)) {
                    result = proxy;
                }
            }
        }
        if (result != null) {
            return result;
        } else {
            LOGGER.warn(String.format("%s has no proxy of type %s! Returning self to avoid issues", this.asBlock().toString(), type.getName()));
            return (Block) (Object) this;
        }
    }

    @Override
    public Block[] getProxies() {
        var array = new Block[proxies.size()];
        for (int i = 0; i < proxies.size(); i++) {
            array[i] = proxies.get(i);
        }
        return array;
    }
}
