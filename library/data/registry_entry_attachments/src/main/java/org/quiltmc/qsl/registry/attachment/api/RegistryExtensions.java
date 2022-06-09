/*
 * Copyright 2021-2022 QuiltMC
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

package org.quiltmc.qsl.registry.attachment.api;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * Extensions for working with {@link Registry}s.
 */
public final class RegistryExtensions {
	public static <R, T extends R, V1> T register(Registry<R> registry, Identifier id, T toRegister,
			RegistryEntryAttachment<R, V1> attach1, V1 value1) {
		Registry.register(registry, id, toRegister);
		attach1.put(toRegister, value1);
		return toRegister;
	}

	public static <R, T extends R, V1, V2> T register(Registry<R> registry, Identifier id, T toRegister,
			RegistryEntryAttachment<R, V1> attach1, V1 value1,
			RegistryEntryAttachment<R, V2> attach2, V2 value2) {
		Registry.register(registry, id, toRegister);
		attach1.put(toRegister, value1);
		attach2.put(toRegister, value2);
		return toRegister;
	}

	public static <R, T extends R, V1, V2, V3> T register(Registry<R> registry, Identifier id, T toRegister,
			RegistryEntryAttachment<R, V1> attach1, V1 value1,
			RegistryEntryAttachment<R, V2> attach2, V2 value2,
			RegistryEntryAttachment<R, V3> attach3, V3 value3) {
		Registry.register(registry, id, toRegister);
		attach1.put(toRegister, value1);
		attach2.put(toRegister, value2);
		attach3.put(toRegister, value3);
		return toRegister;
	}

	public static <R, T extends R, V1, V2, V3, V4> T register(Registry<R> registry, Identifier id, T toRegister,
			RegistryEntryAttachment<R, V1> attach1, V1 value1,
			RegistryEntryAttachment<R, V2> attach2, V2 value2,
			RegistryEntryAttachment<R, V3> attach3, V3 value3,
			RegistryEntryAttachment<R, V4> attach4, V4 value4) {
		Registry.register(registry, id, toRegister);
		attach1.put(toRegister, value1);
		attach2.put(toRegister, value2);
		attach3.put(toRegister, value3);
		attach4.put(toRegister, value4);
		return toRegister;
	}
}
