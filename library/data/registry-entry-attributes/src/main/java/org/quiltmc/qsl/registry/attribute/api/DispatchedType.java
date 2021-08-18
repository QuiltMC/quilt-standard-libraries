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

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;
import java.util.Map;

/**
 * Utility interface used for {@link RegistryEntryAttribute#createDispatched(RegistryKey, Identifier, Map, DispatchedType)}.<p>
 *
 * This allows for polymorphic attribute types!
 */
public interface DispatchedType {
	/**
	 * Gets this instance's type as a string.
	 *
	 * @return type string
	 */
	String getType();
}
