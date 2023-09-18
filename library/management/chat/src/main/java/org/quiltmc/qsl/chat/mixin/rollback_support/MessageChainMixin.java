/*
 * Copyright 2023 The Quilt Project
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

import java.util.logging.Logger;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.encryption.Signer;
import net.minecraft.network.message.MessageChain;
import net.minecraft.network.message.MessageLink;

import org.quiltmc.qsl.chat.api.MessageChainLookup;
import org.quiltmc.qsl.chat.api.ChatSecurityRollbackSupport;

@Mixin(MessageChain.class)
public class MessageChainMixin implements ChatSecurityRollbackSupport {
	private static final Logger quilt$rollbackSupport$logger = Logger.getLogger("QuiltChat|Message Chain Rollback");
	private boolean quilt$rollbackSupport$hasSavedState = false;

	@Shadow
	private @Nullable MessageLink link;

	private MessageLink quilt$rollbackSupport$link;


	@Override
	public void saveState() {
		if (quilt$rollbackSupport$hasSavedState) {
			quilt$rollbackSupport$logger.warning("Saving a rollback state without dropping or using a previous rollback state!");
		}

		this.quilt$rollbackSupport$link = link;

		quilt$rollbackSupport$hasSavedState = true;
	}

	@Override
	public void rollbackState() {
		if (!quilt$rollbackSupport$hasSavedState) {
			throw new IllegalStateException("No rollback state is available!");
		}

		link = quilt$rollbackSupport$link;

		dropSavedState();
	}

	@Override
	public void dropSavedState() {
		if (!quilt$rollbackSupport$hasSavedState) {
			quilt$rollbackSupport$logger.warning("Attempting to drop a state without having one available");
		}

		quilt$rollbackSupport$hasSavedState = false;
	}

	@Inject(
		method = "createPacker",
		at = @At("TAIL")
	)
	public void quilt$captureGeneratedPacker(Signer signer, CallbackInfoReturnable<MessageChain.Packer> cir) {
		MessageChainLookup.registerPacker(cir.getReturnValue(), this);
	}

	@Inject(
		method = "createUnpacker",
		at = @At("TAIL")
	)
	public void quilt$captureGeneratedUnpacker(PlayerPublicKey key, CallbackInfoReturnable<MessageChain.Unpacker> cir) {
		MessageChainLookup.registerUnpacker(cir.getReturnValue(), this);
	}
}
