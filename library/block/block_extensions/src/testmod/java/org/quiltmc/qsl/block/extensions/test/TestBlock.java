package org.quiltmc.qsl.block.extensions.test;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.quiltmc.qsl.block.extensions.api.QuiltBlock;

public class TestBlock extends Block {

    public TestBlock(Settings settings, Block... proxies) {
        super(QuiltBlock.createProxy(settings, proxies));
        this.setDefaultState(QuiltBlock.mergeStates(this, Block::getDefaultState));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        var prop = QuiltBlock.getProperty(state, StairsBlock.HALF);
        if (prop != null) {
            player.sendMessage(new LiteralText(prop.asString()), false);
            return ActionResult.success(world.isClient);
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }
}
