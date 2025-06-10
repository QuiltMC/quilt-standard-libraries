package org.quiltmc.qsl.entity.extensions.mixin.accessor;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.apache.commons.lang3.tuple.Pair;
import org.quiltmc.qsl.entity.extensions.impl.WanderingTraderOffersManager;

import net.minecraft.registry.RegistryKey;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(TradeOffers.class)
public interface TradeOffersAccessor {
    /**
     * Only for use in {@link WanderingTraderOffersManager}
     */
    @Accessor("WANDERING_TRADER_TRADES")
    static List<Pair<TradeOffers.Factory[], Integer>> quilt$getWANDERING_TRADER_TRADES() {
        throw new AssertionError("dummy method body reached");
    }

    /**
     * Only for use in {@link WanderingTraderOffersManager}
     */
    @Accessor("WANDERING_TRADER_TRADES")
    static void quilt$setWANDERING_TRADER_TRADES(List<Pair<TradeOffers.Factory[], Integer>> trades) {
        throw new AssertionError("dummy method body reached");
    }

    @Accessor("PROFESSION_TO_LEVELED_TRADE")
    static Map<RegistryKey<VillagerProfession>, Int2ObjectMap<TradeOffers.Factory[]>>
    quilt$getPROFESSION_TO_LEVELED_TRADE() {
        throw new AssertionError("dummy method body reached");
    }

    @Accessor("EXPERIMENTAL_TRADES")
    static Map<RegistryKey<VillagerProfession>, Int2ObjectMap<TradeOffers.Factory[]>>
    quilt$getEXPERIMENTAL_TRADES() {
        throw new AssertionError("dummy method body reached");
    }
}
