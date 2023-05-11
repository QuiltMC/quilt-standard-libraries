package org.quiltmc.qsl.chat.mixin;

import net.minecraft.network.message.AcknowledgedMessage;
import net.minecraft.network.message.LastSeenMessageTracker;
import net.minecraft.network.message.MessageSignature;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.chat.impl.mixin.LastSeenMessageTrackerRollbackSupport;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.logging.Logger;

@Mixin(LastSeenMessageTracker.class)
public class LastSeenMessageTrackerMixin implements LastSeenMessageTrackerRollbackSupport {
	private static final Logger quilt$rollbackSupport$logger = Logger.getLogger("Quilt Chat|Message Tracker Rollback");
	private boolean quilt$rollbackSupport$hasSavedState = false;

	@Mutable
	@Shadow
	@Final
	private AcknowledgedMessage[] messages;
	@Shadow
	private int nextIndex;
	@Shadow
	private int messageCount;
	@Shadow
	@Nullable
	private MessageSignature signature;

	// Fields from LastSeenMessageTracker
	private AcknowledgedMessage[] quilt$rollbackSupport$messages;
	private int quilt$rollbackSupport$nextIndex;
	private int quilt$rollbackSupport$messageCount;
	@Nullable
	private MessageSignature quilt$rollbackSupport$signature;

	@Override
	public void saveState() {
		if (quilt$rollbackSupport$hasSavedState) {
			quilt$rollbackSupport$logger.warning("Saving a rollback state without dropping or using a previous rollback state!");
		}

		this.quilt$rollbackSupport$messages = messages;
		this.quilt$rollbackSupport$nextIndex = nextIndex;
		this.quilt$rollbackSupport$messageCount = messageCount;
		this.quilt$rollbackSupport$signature = signature;

		this.quilt$rollbackSupport$hasSavedState = true;
	}

	@Override
	public void rollbackState() {
		if (!quilt$rollbackSupport$hasSavedState) {
			throw new IllegalStateException("No rollback state is available!");
		}

		messages = this.quilt$rollbackSupport$messages;
		nextIndex = this.quilt$rollbackSupport$nextIndex;
		messageCount = this.quilt$rollbackSupport$messageCount;
		signature = this.quilt$rollbackSupport$signature;

		quilt$rollbackSupport$hasSavedState = false;
	}

	@Override
	public void dropSavedState() {
		if (!quilt$rollbackSupport$hasSavedState) {
			quilt$rollbackSupport$logger.warning("Attempting to drop a state without having one available");
		}

		quilt$rollbackSupport$hasSavedState = false;
	}
}
