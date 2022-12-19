package org.quiltmc.qsl.block.content.registry.api.enchanting;

import java.util.Optional;

import com.mojang.serialization.Codec;

/**
 * A type to identify booster variations by.
 *
 * @param codec         the codec for the booster
 * @param simpleVariant the default version of the booster when only identified by the type id
 */
public record EnchantingBoosterType(Codec<? extends EnchantingBooster> codec, Optional<EnchantingBooster> simpleVariant) {
}
