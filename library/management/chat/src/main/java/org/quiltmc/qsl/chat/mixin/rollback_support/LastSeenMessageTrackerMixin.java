/*
 * Copyright 2023 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.chat.mixin.rollback_support;

import net.minecraft.network.message.AcknowledgedMessage;
import net.minecraft.network.message.LastSeenMessageTracker;
import net.minecraft.network.message.MessageSignature;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.chat.api.ChatSecurityRollbackSupport;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.logging.Logger;

@Mixin(LastSeenMessageTracker.class)
public class LastSeenMessageTrackerMixin implements ChatSecurityRollbackSupport {
	private static final Logger quilt$rollbackSupport$logger = Logger.getLogger("QuiltChat|Message Tracker Rollback");
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
	private @Nullable MessageSignature signature;

	private AcknowledgedMessage[] quilt$rollbackSupport$messages;
	private int quilt$rollbackSupport$nextIndex;
	private int quilt$rollbackSupport$messageCount;
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

		dropSavedState();
	}

	@Override
	public void dropSavedState() {
		if (!quilt$rollbackSupport$hasSavedState) {
			quilt$rollbackSupport$logger.warning("Attempting to drop a state without having one available");
		}

		quilt$rollbackSupport$hasSavedState = false;
	}
}
