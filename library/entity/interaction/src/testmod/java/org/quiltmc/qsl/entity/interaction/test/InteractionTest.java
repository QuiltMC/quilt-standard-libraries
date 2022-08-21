package org.quiltmc.qsl.entity.interaction.test;

import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.entity.interaction.api.LivingEntityAttackCallback;
import org.quiltmc.qsl.entity.interaction.api.player.AttackEntityCallback;
import org.quiltmc.qsl.entity.interaction.api.player.UseEntityCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteractionTest implements ModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("qsl_entity_interaction_testmod");

	@Override
	public void onInitialize(ModContainer mod) {
		AttackEntityCallback.EVENT.register((player, world, hand, entity) -> {
			if (player.getStackInHand(hand).isOf(Items.DIAMOND_SWORD)) {
				return ActionResult.FAIL;
			}
			if (player.getStackInHand(hand).isOf(Items.DIAMOND_SHOVEL)) {
				return ActionResult.SUCCESS;
			}
			return ActionResult.PASS;
		});

		LivingEntityAttackCallback.EVENT.register((attacker, target, source, amount) -> {
			if (attacker instanceof ZombieEntity) {
				return ActionResult.FAIL;
			}
			return ActionResult.PASS;
		});

		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			System.out.println("Player: " + player + "\nWorld: " + world + "\nEntity: " + entity);
			if (entity instanceof CreeperEntity) return ActionResult.FAIL;
			return ActionResult.PASS;
		});

		LOGGER.info("Finished Interaction Module test init");
	}
}
