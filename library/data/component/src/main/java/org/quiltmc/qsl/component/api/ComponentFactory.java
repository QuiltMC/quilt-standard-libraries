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

/**
 * Class meant to provide a component using the provided {@link ComponentCreationContext} argument.
 *
 * @param <T> The type of the returned component.
 * @author 0xJoeMama
 */
@FunctionalInterface
public interface ComponentFactory<T> {
	/**
	 * @param ctx The {@link ComponentCreationContext} that can be used by the component.
	 * @return A {@link T} instance.
	 */
	T create(ComponentCreationContext ctx);
}
