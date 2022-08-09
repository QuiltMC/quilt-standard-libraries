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

package org.quiltmc.qsl.component.api.provider;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import org.quiltmc.qsl.base.api.util.InjectedInterface;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.Components;
import org.quiltmc.qsl.component.api.container.ComponentContainer;

/**
 * Any object that wishes to allow components to be attached to and queried from it,
 * needs to implement this interface.<br/>
 *
 * <p>
 * Currectly it consists of the {@link ComponentProvider#getComponentContainer()} method, which is the abstract one,
 * as well as the {@link ComponentProvider#expose(ComponentType)} method, which is just a utility method.<br/>
 * In the future, more methods may be added, but for now this is all the API we need to expose on game objects.<br/>
 * You may create custom implementations of this interface. However, to do so, I would suggest looking at
 * {@link ComponentContainer} beforehand.
 *
 * <p>
 * By default, QSL provides the following, interface injected {@linkplain ComponentProvider providers}:
 * <ul>
 *     <li>BlockEntity</li>
 *     <li>Entity</li>
 *     <li>Chunk</li>
 *     <li>World</li>
 *     <li>Level(in other words a world save)</li>
 * </ul>
 *
 * <p>
 * Furthermore, all of them support component saving and syncing.<br/>
 *
 * <b>Note: You shouldn't have to use the {@link ComponentProvider#getComponentContainer()} method that often,
 * if ever, because it's needed for implementations not for using the API.</b>
 *
 * @author 0xJoeMama
 * @see ComponentContainer
 * @see Components
 */
@InjectedInterface({ // We inject this inteface, so that modders don't need to use the methods in Components directly with our default implementations.
		Entity.class,
		BlockEntity.class,
		Chunk.class,
		MinecraftServer.class, // MinecraftServer and MinecraftClient contain Level components
		MinecraftClient.class,
		World.class
})
public interface ComponentProvider {
	/**
	 * Every {@linkplain ComponentProvider provider} must provide a {@link ComponentContainer},
	 * so that it can store the components targetting or that are manually added to it.
	 *
	 * @return An instance of {@link ComponentContainer} with a valid implementation of its interface.
	 */
	default ComponentContainer getComponentContainer() {
		throw new AbstractMethodError("You need to implement the getComponentContainer method on your provider!");
	}

	/**
	 * Utility method to call {@link Components#expose(ComponentType, Object)} on the actual provider.
	 *
	 * @param type The {@linkplain ComponentType type} we want to query.
	 * @param <C>  The type of the held component.
	 * @return A {@link Nullable} object following the rules defined in {@link Components#expose(ComponentType, Object)}.
	 */
	@Nullable
	default <C> C expose(ComponentType<C> type) {
		return this.getComponentContainer().expose(type);
	}

	@Nullable
	default <C> C ifPresent(ComponentType<C> type, Consumer<? super C> action) {
		C component = this.expose(type);
		if (component != null) {
			action.accept(component);
		}

		return component;
	}
}
