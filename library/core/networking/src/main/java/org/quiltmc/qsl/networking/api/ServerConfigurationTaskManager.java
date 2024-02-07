/*
 * Copyright 2024 The Quilt Project
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

package org.quiltmc.qsl.networking.api;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.ServerConfigurationPacketHandler;
import net.minecraft.network.configuration.ConfigurationTask;

import org.quiltmc.qsl.base.api.util.InjectedInterface;

/**
 * An injected interface for {@link net.minecraft.network.ServerConfigurationPacketHandler} that exposes the task system.
 */
@InjectedInterface(ServerConfigurationPacketHandler.class)
public interface ServerConfigurationTaskManager {
	/**
	 * Adds a task to the handler that must complete before joining.
	 *
	 * @param task the task to add
	 */
	void addTask(ConfigurationTask task);

	/**
	 * Adds a task to the handler that must complete before joining.
	 * This task will be executed next regardless of whatever other tasks exist.
	 * This method is useful when you have one task you want to run right after another finishes, like in a state machine.
	 * This will throw if there is already an immediate task to run.
	 *
	 * @param task the task to add
	 */
	void addImmediateTask(ConfigurationTask task);

	/**
	 * Adds a task with a higher priority to the handler that must complete before joining.
	 * Priority tasks are executed in the order they are added to the handler.
	 * All priority tasks will be finished before normal tasks begin executing again.
	 * If a priority task is added during normal task execution, the handler will switch to executing the priority tasks.
	 *
	 * <p>
	 * Calling this during the {@link ServerConfigurationConnectionEvents#INIT} event might add your tasks before registry sync.
	 *
	 * @param task the task to add
	 */
	void addPriorityTask(ConfigurationTask task);

	/**
	 * Finishes the task of the specified type. Will throw an error if a different or no task is running.
	 *
	 * @param type the type to finish
	 */
	void finishTask(ConfigurationTask.Type type);

	/**
	 * Gets the currently running task for the configuration handler.
	 *
	 * @return the current task
	 */
	@Nullable ConfigurationTask getCurrentTask();
}
