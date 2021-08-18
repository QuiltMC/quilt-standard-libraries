package org.quiltmc.qsl.registry.attributes.test;

import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;

import org.quiltmc.qsl.registry.attribute.api.RegistryItemAttribute;
import org.quiltmc.qsl.registry.attribute.api.RegistryItemAttributeHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class SimpleAttributeTest implements ModInitializer {
	public static final RegistryItemAttribute<Item, Integer> TEST_ATTRIBUTE =
			RegistryItemAttribute.create(Registry.ITEM_KEY,
					new Identifier("quilt", "test_attribute"),
					Codec.INT);

	@Override
	public void onInitialize() {
		Registry.register(Registry.ITEM, new Identifier("quilt", "simple_attribute_test_item"),
				new MyItem(new Item.Settings()));
	}

	public static final class MyItem extends Item {
		public MyItem(Settings settings) {
			super(settings);
		}

		@Override
		public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
			if (!world.isClient) {
				RegistryItemAttributeHolder.get(Registry.ITEM_KEY)
						.getValue(this, TEST_ATTRIBUTE)
						.ifPresentOrElse(i -> {
							user.sendMessage(new LiteralText("Item attribute = " + i), true);
						}, () -> {
							throw new RuntimeException("agh, test attribute wasn't applied after all...");
						});
			}
			return TypedActionResult.pass(user.getStackInHand(hand));
		}
	}
}
