/**
 * <h2>The Quilt Item Setting sub API.</h2>
 *
 * <p>
 * <h3>What are {@link net.minecraft.item.Item.Settings}?</h3>
 * {@link net.minecraft.item.Item.Settings} are ways to add specific traits to {@link net.minecraft.item.Item}s without creating a subclass.
 * In addition, these traits are applicable to all {@link net.minecraft.item.Item}s, not just a specific subclass.
 * <br/>
 * This API adds two new settings for items, {@link org.quiltmc.qsl.item.api.item.setting.QuiltCustomItemSettings#CUSTOM_DAMAGE_HANDLER} and {@link org.quiltmc.qsl.item.api.item.setting.QuiltCustomItemSettings#EQUIPMENT_SLOT_PROVIDER}.
 * <br/>
 * These custom settings make use of the {@link org.quiltmc.qsl.item.api.item.setting.CustomItemSetting} API provided.
 * This API allows mods to specify their own custom settings in an API.
 *
 *
 */

package org.quiltmc.qsl.item.api.item.setting;
