/*
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.entity.multipart.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;

/**
 * Represents an entity that contains multiple {@link EntityPart}s.
 * <p>
 * The natural uses for multipart entities is for entities to have specific hitboxes with damage multipliers
 * or for large {@link Entity entities} to have more accurate hitboxes.
 * <p>
 * NOTE: When instantiating {@link EntityPart}s, on the client, make sure to call {@link Entity#setId(int)}.
 *
 * <pre>{@code
 * @Override
 * public void onSpawnPacket(EntitySpawnS2CPacket packet) {
 *     super.onSpawnPacket(packet);
 *     var entityParts = this.getEntityParts();
 *
 *     // We make sure not to override the base entity id
 *     // Mojang did this on the ender dragon, and it caused very janky hitboxes
 *     for(int i = 1; i <= entityParts.length; i++) {
 *         entityParts[i].setId(i + packet.getId());
 *     }
 * }
 * }</pre>
 *
 * <p>
 * When moving this {@link Entity}, do note that you should also change the position of the child {@link EntityPart}s too.
 * This also includes updating their {@link Entity#prevX prevX}, {@link Entity#prevY prevY}, {@link Entity#prevZ prevZ},
 * {@link Entity#lastRenderX lastRenderX}, {@link Entity#lastRenderY lastRenderY}, and {@link Entity#lastRenderZ lastRenderZ}.
 *
 * @see EnderDragonEntity
 */
public interface MultipartEntity {
	EntityPart<?>[] getEntityParts();
}
