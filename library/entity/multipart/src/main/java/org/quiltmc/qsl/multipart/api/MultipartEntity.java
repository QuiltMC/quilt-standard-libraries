/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.multipart.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;

/**
 * Represents an entity that contains multiple {@link EntityPart}s.
 * <p>
 * The natural uses for multipart entities is for entities to have specific hitboxes with damage multipliers
 * or for large {@link Entity entities} to have more accurate hitboxes.</p>
 * <p>
 * When moving this {@link Entity}, do note that you should also change the position of the child {@link EntityPart}s too.
 * This also includes updating their {@link Entity#prevX}, {@link Entity#prevY}, {@link Entity#prevZ},
 * {@link Entity#lastRenderX}, {@link Entity#lastRenderY}, and {@link Entity#lastRenderZ}.</p>
 * @see EnderDragonEntity
 */
public interface MultipartEntity {
	EntityPart<?>[] getEntityParts();
}
