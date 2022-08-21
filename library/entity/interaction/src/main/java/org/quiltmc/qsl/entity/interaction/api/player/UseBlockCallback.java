package org.quiltmc.qsl.entity.interaction.api.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.quiltmc.qsl.base.api.event.Event;

/**
 * Invoked whenever a player interacts (right clicks) a block.
 *
 * <p>Upon return:
 * <ul><li>SUCCESS cancels further processing and, on the client, sends a packet to the server.
 * <li>PASS falls back to further processing.
 * <li>FAIL cancels further processing and does not send a packet to the server.</ul>
 */
@FunctionalInterface
public interface UseBlockCallback {

	Event<UseBlockCallback> EVENT = Event.create(UseBlockCallback.class,
			callbacks -> (player, world, hand, hitResult) -> {
				for (UseBlockCallback callback : callbacks) {
					ActionResult result = callback.onUseBlock(player, world, hand, hitResult);

					if (result != ActionResult.PASS) return result;
				}
				return ActionResult.PASS;
			});

	ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult);
}
