package org.quiltmc.qsl.item.api.item.v1;

import org.quiltmc.qsl.item.impl.CustomItemSettingImpl;

/**
 * A list of the {@link CustomItemSetting}s that are provided by Quilt
 */
public final class QuiltCustomItemSettings {
	private QuiltCustomItemSettings() {
	}

	/**
	 * The {@link CustomItemSetting} in charge of handing {@link EquipmentSlotProvider}s
	 */
	public static final CustomItemSetting<EquipmentSlotProvider> EQUIPMENT_SLOT_PROVIDER = CustomItemSettingImpl.EQUIPMENT_SLOT_PROVIDER;
	/**
	 * The {@link CustomItemSetting} in charge of handing {@link CustomDamageHandler}s
	 */
	public static final CustomItemSetting<CustomDamageHandler> CUSTOM_DAMAGE_HANDLER = CustomItemSettingImpl.CUSTOM_DAMAGE_HANDLER;
	/**
	 * The {@link CustomItemSetting} in charge of handing {@link RecipeRemainderProvider}s. This setting should be used when implementing custom crafting systems to properly handle remainders.
	 */
	public static final CustomItemSetting<RecipeRemainderProvider> RECIPE_REMAINDER_PROVIDER = CustomItemSettingImpl.RECIPE_REMAINDER_PROVIDER;
}
