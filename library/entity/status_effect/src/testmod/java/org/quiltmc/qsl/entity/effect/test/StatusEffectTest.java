package org.quiltmc.qsl.entity.effect.test;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.entity.effect.api.StatusEffectRemovalReason;

public final class StatusEffectTest implements ModInitializer {
	public static final String NAMESPACE = "quilt_status_effect_testmod";

	public static Identifier id(String path) {
		return new Identifier(NAMESPACE, path);
	}

	public static final StatusEffectRemovalReason DRANK_PASTEURIZED_MILK = new StatusEffectRemovalReason(id("action.drank_pasteurized_milk")) {
		@Override
		public boolean removesEffect(StatusEffectInstance effect) {
			return effect.getEffectType().getType() == StatusEffectType.HARMFUL;
		}
	};

	public static final Item PASTEURIZED_MILK_BUCKET = Registry.register(Registry.ITEM, id("pasteurized_milk_bucket"),
			new PasteurizedMilkBucketItem(new Item.Settings()
					.recipeRemainder(Items.BUCKET)
					.maxCount(1)
					.group(ItemGroup.MISC)));

	@Override
	public void onInitialize(ModContainer mod) {}
}
