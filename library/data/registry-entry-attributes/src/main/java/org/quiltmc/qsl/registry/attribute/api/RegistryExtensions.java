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

import org.quiltmc.qsl.registry.attribute.impl.BuiltinRegistryEntryAttributeSetterImpl;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import java.util.function.Consumer;

public final class RegistryExtensions {
	public static <R, T extends R> T registerWithAttributes(Registry<R> registry, Identifier id, T entry,
												  Consumer<AttributeSetter<R>> setterConsumer) {
		Registry.register(registry, id, entry);
		setterConsumer.accept(new BuiltinRegistryEntryAttributeSetterImpl<>(registry, entry));
		return entry;
	}

	public interface AttributeSetter<R> {
		<T> AttributeSetter<R> put(RegistryEntryAttribute<R, T> attrib, T value);
	}
}
