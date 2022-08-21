package org.quiltmc.qsl.entity.interaction.api.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.quiltmc.qsl.base.api.event.Event;

import javax.annotation.Nullable;

@FunctionalInterface
public interface UseEntityCallback {
	Event<UseEntityCallback> EVENT = Event.create(UseEntityCallback.class,
			callbacks -> (player, world, hand, entity, hitResult) -> {
				for (UseEntityCallback callback : callbacks) {
					ActionResult result = callback.onUseEntity(player, world, hand, entity, hitResult);

					if (result != ActionResult.PASS) return result;
				}
				return ActionResult.PASS;
			});


	ActionResult onUseEntity(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult);
}
