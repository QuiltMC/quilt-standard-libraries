package org.quiltmc.qsl.chat.api.types;

import net.minecraft.entity.player.PlayerEntity;

abstract public class MutableAbstractMessage<T extends ImmutableAbstractMessage<T, S>, S> extends ImmutableAbstractMessage<T, S> {
	public MutableAbstractMessage(PlayerEntity player, boolean isOnClientSide) {
		super(player, isOnClientSide);
	}
}
