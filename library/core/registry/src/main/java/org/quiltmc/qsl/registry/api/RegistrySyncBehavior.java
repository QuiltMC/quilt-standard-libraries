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

package org.quiltmc.qsl.registry.api;

/**
 * Specifies the behavior for synchronizing a registry and its contents.
 */
public enum RegistrySyncBehavior {
	/**
	 * The registry <em>will not</em> be synchronized to the client.
	 */
	SKIPPED,
	/**
	 * The registry <em>will</em> be synchronized to the client,
	 * and clients who do not have this registry on their side <em>will</em> be kicked.
	 */
	REQUIRED,
	/**
	 * The registry <em>will</em> be synchronized to the client,
	 * and clients who do not have this registry on their side <em>will not</em> be kicked.
	 */
	OPTIONAL
}
