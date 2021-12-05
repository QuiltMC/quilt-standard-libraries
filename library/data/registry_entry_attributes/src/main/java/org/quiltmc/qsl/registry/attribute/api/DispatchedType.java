/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.registry.attribute.api;

import java.util.function.Function;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * Utility interface used for {@link RegistryEntryAttribute#dispatchedBuilder(Registry, Identifier, Function)}.
 *
 * <p>
 * This allows for polymorphic attribute types!<br>
 * For example, say you have this interface:
 * <pre><code>
 * public interface Behavior extends DispatchedType {
 *     void execute(ServerPlayerEntity player);
 * }
 * </code></pre>
 * <p>
 * Using the {@code createDispatched} method, you can create an attribute for a composable behavior:
 * <pre><code>
 * public static final SimpleRegistry&lt;Codec&lt;? extends Behavior&gt;&gt; REGISTRY = new SimpleRegistry&lt;&gt;();
 * public static final RegistryEntryAttribute&lt;Item, Behavior&gt; ATTRIBUTE =
 *     RegistryEntryAttribute.&lt;Item, Behavior&gt;dispatchedBuilder(Registry.ITEM, id("behavior"), REGISTRY::get).build();
 *
 * public static void onItemUsed(ServerPlayerEntity player, ItemStack stack) {
 *     ATTRIBUTE.getValue(stack.getItem()).ifPresent(behavior -> behavior.execute(player));
 * }
 * </code></pre>
 */
public interface DispatchedType {
	/**
	 * Gets this instance's type.
	 * <p>
	 * This is used by the {@linkplain com.mojang.serialization.Codec#dispatch(Function, Function) dispatched codec}
	 * to get the {@code Codec} instance used to (de)serialize instances of this type.
	 *
	 * @return type identifier
	 */
	Identifier getType();
}
