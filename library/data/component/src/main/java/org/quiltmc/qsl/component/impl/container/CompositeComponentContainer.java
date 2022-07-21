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

public class CompositeComponentContainer implements ComponentContainer {
	private final ComponentContainer main;
	private final ComponentContainer fallback;

	public CompositeComponentContainer(ComponentContainer main, ComponentContainer fallback) {
		this.main = main;
		this.fallback = fallback;
	}

	@Override
	public Maybe<Component> expose(ComponentType<?> type) {
		return this.main.expose(type).or(() -> this.fallback.expose(type));
	}

	@Override
	public void writeNbt(NbtCompound providerRootNbt) {
		this.main.writeNbt(providerRootNbt);
		this.fallback.writeNbt(providerRootNbt);
	}

	@Override
	public void readNbt(NbtCompound providerRootNbt) {
		this.main.readNbt(providerRootNbt);
		this.fallback.readNbt(providerRootNbt);
	}

	@Override
	public void tick(ComponentProvider provider) {
		this.main.tick(provider);
		this.fallback.tick(provider);
	}

	@Override
	public void sync(ComponentProvider provider) {
		this.main.sync(provider);
		this.fallback.sync(provider);
	}

	@Override
	public void forEach(BiConsumer<ComponentType<?>, ? super Component> action) {
		this.main.forEach(action);
		this.fallback.forEach(action);
	}
}
