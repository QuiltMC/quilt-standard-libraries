package org.quiltmc.qsl.chat.impl.mixin;

import net.minecraft.network.message.LastSeenMessageTracker;

/**
 * Implements rollback support for {@link LastSeenMessageTracker#update()} so that we can "unsign" outbound messages
 * <p>
 * Signatures don't become invalid, but they are removed from the message tracker so that future ones can
 * be properly signed. Mostly used for message cancellation.
 */
public interface LastSeenMessageTrackerRollbackSupport {
	void saveState();
	void rollbackState();
	void dropSavedState();
}
