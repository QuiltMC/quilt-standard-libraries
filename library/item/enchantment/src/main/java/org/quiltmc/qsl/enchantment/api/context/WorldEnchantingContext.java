package org.quiltmc.qsl.enchantment.api.context;

import org.jetbrains.annotations.Contract;

import net.minecraft.item.ItemStack;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.World;

/**
 * A class that contains contextual information about the enchantment process.
 * <p>
 * Contains further information about the world the enchanting is happening in.
 */
public class WorldEnchantingContext extends EnchantingContext {
	protected final World world;

	public WorldEnchantingContext(int level, int power, ItemStack stack, World world, RandomGenerator random, boolean treasureAllowed) {
		super(level, power, stack, random, treasureAllowed);
		this.world = world;
	}

	@Contract("_->new")
	public EnchantingContext withLevel(int level) {
		return new WorldEnchantingContext(level, this.power, this.stack, this.world, this.random, this.treasureAllowed);
	}

	@Contract("_->new")
	public EnchantingContext withPower(int power) {
		return new WorldEnchantingContext(this.level, power, this.stack, this.world, this.random, this.treasureAllowed);
	}

	@Contract("_,_,_->new")
	public EnchantingContext withCoreContext(ItemStack stack, RandomGenerator random, boolean treasureAllowed) {
		return new WorldEnchantingContext(this.level, this.power, stack, this.world, random, treasureAllowed);
	}

	/**
	 * @return the world in which the item is being enchanted
	 */
	public World getWorld() {
		return this.world;
	}
}
