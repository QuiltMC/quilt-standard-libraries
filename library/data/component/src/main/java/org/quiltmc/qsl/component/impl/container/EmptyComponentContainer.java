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

package org.quiltmc.qsl.component.impl.container;

import net.minecraft.nbt.NbtCompound;
import org.quiltmc.qsl.base.api.util.Maybe;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;

import java.util.function.BiConsumer;

public final class EmptyComponentContainer implements ComponentContainer {
	public static final EmptyComponentContainer INSTANCE = new EmptyComponentContainer();

	public static final ComponentContainer.Factory<EmptyComponentContainer> FACTORY =
			(provider, injections, saveOperation, ticking, syncChannel) -> INSTANCE;

	private EmptyComponentContainer() { }

	@Override
	public Maybe<Component> expose(ComponentType<?> type) {
		return Maybe.nothing();
	}

	@Override
	public void writeNbt(NbtCompound providerRootNbt) { }

	@Override
	public void readNbt(NbtCompound providerRootNbt) { }

	@Override
	public void tick(ComponentProvider provider) { }

	@Override
	public void sync(ComponentProvider provider) { }

	@Override
	public void forEach(BiConsumer<ComponentType<?>, ? super Component> action) { }
}
