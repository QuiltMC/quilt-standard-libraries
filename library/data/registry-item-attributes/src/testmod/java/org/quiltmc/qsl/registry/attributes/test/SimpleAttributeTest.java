package org.quiltmc.qsl.registry.attributes.test;

import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;

import org.quiltmc.qsl.registry.attribute.api.RegistryExtensions;
import org.quiltmc.qsl.registry.attribute.api.RegistryItemAttribute;
import org.quiltmc.qsl.registry.attribute.api.RegistryItemAttributeHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class SimpleAttributeTest implements ModInitializer {
	public static final RegistryItemAttribute<Item, Integer> TEST_ATTRIBUTE =
			RegistryItemAttribute.createInt(Registry.ITEM_KEY,
					new Identifier("quilt", "test_attribute"));
	public static final RegistryItemAttribute<Item, Float> TEST_ATTRIBUTE_2 =
			RegistryItemAttribute.createFloat(Registry.ITEM_KEY,
					new Identifier("quilt", "test_attribute_2"));

	@Override
	public void onInitialize() {
		RegistryExtensions.registerWithAttributes(Registry.ITEM,
				new Identifier("quilt", "simple_attribute_test_item"),
				new MyItem(new Item.Settings()),
				setter -> setter
						.put(TEST_ATTRIBUTE, 5)		// this value will be overriden by the value specified in the datapack
						.put(TEST_ATTRIBUTE_2, 2.0f));
	}

	public static final class MyItem extends Item {
		public MyItem(Settings settings) {
			super(settings);
		}

		@Override
		public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
			if (!world.isClient) {
				var holder = RegistryItemAttributeHolder.get(Registry.ITEM_KEY);
				int one = holder.getValue(this, TEST_ATTRIBUTE)
						.orElseThrow(() -> new RuntimeException(TEST_ATTRIBUTE + " not set via datapack!"));
				float two = holder.getValue(this, TEST_ATTRIBUTE_2)
						.orElseThrow(() -> new RuntimeException(TEST_ATTRIBUTE_2 + " not set via built-in!"));
				user.sendMessage(Text.of("Test1 = " + one + ", Test2 = " + two), true);
			}
			return TypedActionResult.pass(user.getStackInHand(hand));
		}
	}
}
