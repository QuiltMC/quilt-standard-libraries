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

package org.quiltmc.qsl.chat.mixin;

import net.minecraft.network.message.AcknowledgedMessage;
import net.minecraft.network.message.LastSeenMessageTracker;
import net.minecraft.network.message.MessageSignature;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.quiltmc.qsl.chat.impl.mixin.LastSeenMessageTrackerRollbackSupport;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.logging.Logger;

@Mixin(LastSeenMessageTracker.class)
public class LastSeenMessageTrackerMixin implements LastSeenMessageTrackerRollbackSupport {
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

	// Fields from LastSeenMessageTracker
	private AcknowledgedMessage[] quilt$rollbackSupport$messages;
	private int quilt$rollbackSupport$nextIndex;
	private int quilt$rollbackSupport$messageCount;

	@Override
	public void saveState() {
		if (quilt$rollbackSupport$hasSavedState) {
			quilt$rollbackSupport$logger.warning("Saving a rollback state without dropping or using a previous rollback state!");
		}

		this.quilt$rollbackSupport$messages = messages;
		this.quilt$rollbackSupport$nextIndex = nextIndex;
		this.quilt$rollbackSupport$messageCount = messageCount;

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

		quilt$rollbackSupport$hasSavedState = false;
	}

	@Override
	public void dropSavedState() {
		if (!quilt$rollbackSupport$hasSavedState) {
			quilt$rollbackSupport$logger.warning("Attempting to drop a state without having one available");
		}

		quilt$rollbackSupport$hasSavedState = false;
	}

	// TMP DEBUG INJECTIONS: REMOVE THESE

	@Inject(method = "validateSignature", at = @At(
		value = "FIELD",
		target = "Lnet/minecraft/network/message/LastSeenMessageTracker;signature:Lnet/minecraft/network/message/MessageSignature;",
		opcode = Opcodes.PUTFIELD,
		shift = At.Shift.AFTER
	))
	public void debugSignatureWrites(MessageSignature signature, boolean addMessage, CallbackInfoReturnable<Boolean> cir) {
		System.out.println("QC|LSMT|SIG changing signature to " + signature.hashCode());
	}

	@Inject(method = "update", at = @At("HEAD"))
	public void debugLogBeforeUpdate(CallbackInfoReturnable<LastSeenMessageTracker.Update> cir) {
		System.out.println("QC|LSMT|BEFORE messages : " + Arrays.stream(messages).distinct().toList().size());
		System.out.println("QC|LSMT|BEFORE nextIndex: " + nextIndex);
		System.out.println("QC|LSMT|BEFORE msgCount : " + messageCount);
		System.out.println("----------------------------------");
	}

	@Inject(method = "update", at = @At("TAIL"))
	public void debugLogAfterUpdate(CallbackInfoReturnable<LastSeenMessageTracker.Update> cir) {
		var result = cir.getReturnValue();

		System.out.println("QC|LSMT|AFTER messages : " + Arrays.stream(messages).distinct().toList().size());
		System.out.println("QC|LSMT|AFTER nextIndex: " + nextIndex);
		System.out.println("QC|LSMT|AFTER msgCount : " + messageCount);
		System.out.println("QC|LSMT|AFTER Uack     : " + result.acknowledgment());
		System.out.println("QC|LSMT|AFTER signature: " + (signature != null ? signature.hashCode() : 0));
		System.out.println("----------------------------------");
	}
}
