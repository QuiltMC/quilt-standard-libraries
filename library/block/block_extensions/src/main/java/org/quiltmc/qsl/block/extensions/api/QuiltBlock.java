package org.quiltmc.qsl.block.extensions.api;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.block.extensions.impl.BlockExtension;

import java.util.LinkedHashMap;
import java.util.LinkedList;

//TODO: add a way to make any item place-able
//TODO: access widen stairs and other privated blocks by default

public class QuiltBlock extends Block {
    public static final ThreadLocal<Block[]> PROXY_BLOCKS_TEMP_CONTAINER = new ThreadLocal<>();

    public QuiltBlock(Settings settings) {
        super(settings);
    }

    /**
     * Constructor for quickly adding this block to a variant-driven map
     * @param variant   The variant this block corresponds to
     * @param blockMap  The map we want our block added to, needs to be a <{@link StringIdentifiable}, {@link Block}> {@link LinkedHashMap}, a linked map is used here so order of addition is preserved.
     * @param settings  This block's settings
     */
    public QuiltBlock(StringIdentifiable variant, LinkedHashMap<StringIdentifiable, Block> blockMap, Settings settings) {
        this(settings);
        blockMap.put(variant, this);
    }

    /**
     * Constructor for quickly adding this block to a List
     * @param blockList The map we want our block added to, needs to be a <{@link StringIdentifiable}, {@link Block}> {@link LinkedList}, a linked List is used here so order of addition is preserved.
     * @param settings  This block's settings
     */
    public QuiltBlock(LinkedList<Block> blockList, Settings settings) {
        this(settings);
        blockList.add(this);
    }

    /**
     * Constructor for creating a block with "proxy" super blocks
     * @param settings  This block's settings
     * @param proxies   An array of "proxy" blocks
     */
    public QuiltBlock(Settings settings, Block... proxies) {
        this(QuiltBlock.createProxy(settings, proxies));
    }

    /**
     * Creates a proxy from one or multiple blocks (will be temporarily stocked in a {@link ThreadLocal} and used in {@link org.quiltmc.qsl.block.extensions.mixin.MixinBlock})
     * @param settings  This block's settings (dummy usage, to be able to insert logic at {@code  super()} call
     * @param proxies   Array of proxy blocks to be "merged" together as one (property merging)
     * @return          This block's settings
     */
    public static Settings createProxy(Settings settings, Block... proxies) {
        PROXY_BLOCKS_TEMP_CONTAINER.set(proxies);
        return settings;
    }

    /**
     * Checks if this block has been built using {@code blockClass} as a Proxy
     * @param blockClass    The Block class to check this block against
     * @return              boolean telling us weither this block has {@code blockClass} as a proxy or not
     */
    public boolean isInstanceOfProxy(Class<? extends Block> blockClass) {
        return ((BlockExtension) this).isInstanceOf(blockClass);
    }
}
