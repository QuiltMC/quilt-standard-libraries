/**
 * <h2>The Quilt Item Settings API.</h2>
 *
 * <p>
 * <h3>What are {@link net.minecraft.item.Item.Settings}?</h3>
 * {@link net.minecraft.item.Item.Settings} are ways to add specific traits to {@link net.minecraft.item.Item}s without creating a subclass.
 * In addition, these traits are applicable to all {@link net.minecraft.item.Item}s, not just a specific subclass.
 * <p>
 * This API adds three new settings for items,
 * {@link org.quiltmc.qsl.item.setting.api.QuiltItemSettings#customDamage(org.quiltmc.qsl.item.setting.api.CustomDamageHandler)},
 * {@link org.quiltmc.qsl.item.setting.api.QuiltItemSettings#equipmentSlot(org.quiltmc.qsl.item.setting.api.EquipmentSlotProvider)}, and
 * {@link org.quiltmc.qsl.item.setting.api.QuiltItemSettings#recipeRemainder(org.quiltmc.qsl.item.setting.api.RecipeRemainderProvider)}.
 * <p>
 * These custom settings make use of the {@link org.quiltmc.qsl.item.setting.api.CustomItemSetting} API provided.
 * This API allows mods to specify their own custom settings in an API.
 */

package org.quiltmc.qsl.item.setting.api;
