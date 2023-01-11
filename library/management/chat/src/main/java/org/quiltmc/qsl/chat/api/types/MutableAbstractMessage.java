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

package org.quiltmc.qsl.chat.api.types;

import net.minecraft.entity.player.PlayerEntity;

sealed abstract public class MutableAbstractMessage<T extends ImmutableAbstractMessage<T, S>, S> extends ImmutableAbstractMessage<T, S>
		permits ImmutableS2CProfileIndependentMessage, MutableC2SChatMessage, MutableS2CChatMessage, MutableS2CProfileIndependentMessage, MutableS2CSystemMessage {
	public MutableAbstractMessage(PlayerEntity player, boolean isOnClientSide) {
		super(player, isOnClientSide);
	}
}
