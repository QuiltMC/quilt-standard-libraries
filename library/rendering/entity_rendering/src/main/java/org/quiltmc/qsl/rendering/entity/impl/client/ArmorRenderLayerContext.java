package org.quiltmc.qsl.rendering.entity.impl.client;

import net.minecraft.client.render.entity.state.BipedRenderState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.EquipmentAsset;

public record ArmorRenderLayerContext(
    BipedRenderState state, ItemStack stack, EquipmentSlot slot, RegistryKey<EquipmentAsset> armorAsset
) { }
