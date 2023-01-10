package org.quiltmc.qsl.chat.api.types;

import net.minecraft.entity.player.PlayerEntity;

sealed abstract public class MutableAbstractMessage<T extends ImmutableAbstractMessage<T, S>, S> extends ImmutableAbstractMessage<T, S>
		permits ImmutableS2CProfileIndependentMessage, MutableC2SChatMessage, MutableS2CChatMessage, MutableS2CProfileIndependentMessage, MutableS2CSystemMessage {
	public MutableAbstractMessage(PlayerEntity player, boolean isOnClientSide) {
		super(player, isOnClientSide);
	}
}
