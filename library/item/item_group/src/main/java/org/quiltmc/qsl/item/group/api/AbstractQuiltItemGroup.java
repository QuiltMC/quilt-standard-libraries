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

package org.quiltmc.qsl.item.group.api;

import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;

public abstract class AbstractQuiltItemGroup extends ItemGroup {
	public final Identifier identifier;

	public AbstractQuiltItemGroup(int i, Identifier identifier) {
		super(i, String.format("%s.%s", identifier.getNamespace(), identifier.getPath()));

		this.identifier = identifier;
	}
}
