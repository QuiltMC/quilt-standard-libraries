package org.quiltmc.qsl.entity.api.event;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.quiltmc.qsl.base.api.event.ArrayEvent;

/**
 * A pair of callbacks which are invoked when a LivingEntity takes fatal damage.
 *
 * <p>Mods can return true from a callback to keep the entity alive, like totems of undying do. This will also prevent
 * {@link EntityKilledCallback} listeners from being called.
 *
 * <p>Vanilla checks for entity health {@code <= 0} each tick (with
 * {@link net.minecraft.entity.LivingEntity#isDead()}), and kills the entity if this is true - so the entity will
 * still die next tick if this event is cancelled. It's assumed that the listener will do something to prevent this,
 * for example:
 *
 * <ul>
 *     <li>a totem-of-undying mod giving the player a little health + regeneration effect</li>
 *     <li>a minigame mod teleporting the player into a 'respawn room' and setting their health to 20.0</li>
 *     <li>a mod that changes death mechanics switching the player over to the mod's play-mode, where death doesn't
 *     apply</li>
 * </ul>
 *
 * <p>These events are short-circuiting - if the entity has already been revived, your callback will not fire.
 */
public final class EntityReviveEvents {

	/**
	 * Called before totems try to revive the player.
	 */
	public static ArrayEvent<TryReviveBeforeTotem> BEFORE_TOTEM = ArrayEvent.create(TryReviveBeforeTotem.class, callbacks -> (entity, damageSource) -> {
		for (TryReviveBeforeTotem callback : callbacks) {
			if (callback.tryReviveBeforeTotem(entity, damageSource)) {
				return true;
			}
		}

		return false;
	});

	/**
	 * Called after there has been an attempt to revive with totems, but it has not been successful.
	 */
	public static ArrayEvent<TryReviveAfterTotem> AFTER_TOTEM = ArrayEvent.create(TryReviveAfterTotem.class, callbacks -> (entity, damageSource) -> {
		for (TryReviveAfterTotem callback : callbacks) {
			if (callback.tryReviveAfterTotem(entity, damageSource)) {
				return true;
			}
		}

		return false;
	});

	@FunctionalInterface
	public interface TryReviveBeforeTotem {
		/**
		 * Whether an entity which has fatal damage should be revived.
		 *
		 * @param entity       the entity
		 * @param damageSource the fatal damage source
		 * @return true if the entity should be revived, false otherwise.
		 */
		boolean tryReviveBeforeTotem(LivingEntity entity, DamageSource damageSource);
	}


	@FunctionalInterface
	public interface TryReviveAfterTotem {
		/**
		 * Whether an entity which has fatal damage and which has not been saved by a totem should be revived.
		 *
		 * @param entity       the entity
		 * @param damageSource the fatal damage source
		 * @return true if the entity should be revived, false otherwise.
		 */
		boolean tryReviveAfterTotem(LivingEntity entity, DamageSource damageSource);
	}

	private EntityReviveEvents() {}
}
