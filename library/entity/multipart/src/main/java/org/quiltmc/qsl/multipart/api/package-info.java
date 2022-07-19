/**
 * <h2>A Multipart Entity API</h2>
 * <p>
 * This API abstracts many of the previously {@link net.minecraft.entity.boss.dragon.EnderDragonEntity EnderDragon} and
 * {@link net.minecraft.entity.boss.dragon.EnderDragonPart EnderDragonPart} specific code so that modders can implement
 * their own {@link org.quiltmc.qsl.multipart.api.MultipartEntity MultipartEntities}.</p>
 * <p>
 * <h3>Creating a Multipart Entity</h3>
 * To create a {@link org.quiltmc.qsl.multipart.api.MultipartEntity MultipartEntity},
 * your {@link net.minecraft.entity.Entity Entity} simply has to implement the interface and its one method,
 * {@link org.quiltmc.qsl.multipart.api.MultipartEntity#getEntityParts() getEntityParts()}. Then, each of its parts must
 * be an instance of {@link org.quiltmc.qsl.multipart.api.EntityPart EntityPart}. This is made easy with the provided
 * {@link org.quiltmc.qsl.multipart.api.AbstractEntityPart AbstractEntityPart}.</p>
 * <p>
 * <h3>NOTE:</h3>
 * When instantiating {@link org.quiltmc.qsl.multipart.api.EntityPart EntityParts}, on the client,
 * make sure to call {@link net.minecraft.entity.Entity#setId(int) setId(int)}.</p>
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
 * </p>
 * <p>
 * This API also fixes two vanilla bugs:
 * <ul>
 *     <li><a href="https://bugs.mojang.com/browse/MC-158205">MC-158205</a></li>
 *     <li><a href="https://bugs.mojang.com/browse/MC-225055">MC-225055</a></li>
 * </ul>
 * </p>
 */

package org.quiltmc.qsl.multipart.api;
