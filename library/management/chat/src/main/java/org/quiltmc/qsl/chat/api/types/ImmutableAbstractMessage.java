package org.quiltmc.qsl.chat.api.types;

import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.chat.api.QuiltMessageType;

import java.util.EnumSet;

/**
 * @param <T> The type of the immutable type
 * @param <S> The type of the packet form of this class
 */
sealed public abstract class ImmutableAbstractMessage<T extends ImmutableAbstractMessage<T, S>, S>
		permits ImmutableC2SChatMessage, ImmutableS2CChatMessage, ImmutableS2CSystemMessage, MutableAbstractMessage {
	protected final @NotNull PlayerEntity player;
	protected final boolean isOnClientSide;

	protected ImmutableAbstractMessage(@NotNull PlayerEntity player, boolean isOnClientSide) {
		this.player = player;
		this.isOnClientSide = isOnClientSide;
	}

	// These are mostly annotated as @NotNull for dev productivity, I get nice warnings about
	// What parts of the subclasses I need to implement still
	abstract public @NotNull EnumSet<QuiltMessageType> getTypes();

	abstract public @NotNull T immutableCopy();

	abstract public @NotNull S asPacket();

	/**
	 * Returns the player associated with this packet, which changes meaning based on the sidedness and direction.
	 * <p>
	 * For System and Profile Independent messages, the player is the target of the message as players cannot send these to the server.
	 * <p>
	 * For Chat messages, on the server inbound, it is the sender of the chat message, while outbound it is the player the message will be sent to.
	 * Chat messages on the client return the client player.
	 * @return the {@link PlayerEntity} associated with this packet
	 */
	public final @NotNull PlayerEntity getPlayer() {
		return player;
	}
}
