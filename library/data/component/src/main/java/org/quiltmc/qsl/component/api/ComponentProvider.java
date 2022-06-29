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

package org.quiltmc.qsl.component.api;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.level.LevelProperties;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.base.api.util.InjectedInterface;
import org.quiltmc.qsl.component.api.components.FunctionComponent;

import java.util.Optional;

@InjectedInterface({
		Entity.class,
		BlockEntity.class,
		Chunk.class,
		LevelProperties.class,
		ItemStack.class,
		MinecraftClient.class
})
public interface ComponentProvider {
	@NotNull
	ComponentContainer getContainer();

	default <T, U, C extends FunctionComponent<T, U>> Optional<U> call(ComponentType<C> type, T t) {
		return this.expose(type).map(func -> func.call(this, t));
	}

	default <C extends Component> Optional<C> expose(ComponentType<C> id) {
		return Components.expose(id, this);
	}

	default void sync() {
		this.getContainer().sync(this);
	}
}
