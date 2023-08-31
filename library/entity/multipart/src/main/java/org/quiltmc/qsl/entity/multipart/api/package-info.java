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

/**
 * <h2>The Multipart Entity API.</h2>
 * <p>
 * This API abstracts many of the previously {@link net.minecraft.entity.boss.dragon.EnderDragonEntity EnderDragon} and
 * {@link net.minecraft.entity.boss.dragon.EnderDragonPart EnderDragonPart} specific code so that modders can implement
 * their own {@link org.quiltmc.qsl.entity.multipart.api.MultipartEntity MultipartEntities}.
 *
 * <p>
 * <h3>Creating a Multipart Entity</h3>
 * To create a {@link org.quiltmc.qsl.entity.multipart.api.MultipartEntity MultipartEntity},
 * your {@link net.minecraft.entity.Entity Entity} simply has to implement the interface and its one method,
 * {@link org.quiltmc.qsl.entity.multipart.api.MultipartEntity#getEntityParts() getEntityParts()}. Then, each of its parts must
 * be an instance of {@link org.quiltmc.qsl.entity.multipart.api.EntityPart EntityPart}. This is made easy with the provided
 * {@link org.quiltmc.qsl.entity.multipart.api.AbstractEntityPart AbstractEntityPart}.
 * <p>
 * <h3>NOTE:</h3>
 * When instantiating {@link org.quiltmc.qsl.entity.multipart.api.EntityPart EntityParts}, on the client,
 * make sure to call {@link net.minecraft.entity.Entity#setId(int) setId(int)}.
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
 * }}</pre>
 *
 * <p>
 * This API also fixes two vanilla bugs:
 * <ul>
 *     <li><a href="https://bugs.mojang.com/browse/MC-158205">MC-158205</a></li>
 *     <li><a href="https://bugs.mojang.com/browse/MC-225055">MC-225055</a></li>
 * </ul>
 */

package org.quiltmc.qsl.entity.multipart.api;
