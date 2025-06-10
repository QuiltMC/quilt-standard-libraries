package org.quiltmc.qsl.entity.extensions.mixin.accessor;

import org.apache.commons.lang3.tuple.Pair;
import org.quiltmc.qsl.entity.extensions.impl.WanderingTraderOffersManager;

import net.minecraft.village.TradeOffers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(TradeOffers.class)
public interface TradeOffersAccessor {
    /**
     * Only for use in {@link WanderingTraderOffersManager}
     */
    @Mutable
    @Accessor("WANDERING_TRADER_TRADES")
    static void quilt$setWANDERING_TRADER_TRADES(List<Pair<TradeOffers.Factory[], Integer>> trades) {
        throw new AssertionError("dummy method body reached");
    }
}
