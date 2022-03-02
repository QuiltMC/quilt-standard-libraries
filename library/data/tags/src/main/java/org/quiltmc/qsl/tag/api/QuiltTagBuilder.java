/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.tag.api;

import net.minecraft.tag.Tag;

import org.quiltmc.qsl.base.api.util.InjectedInterface;

/**
 * Interface implemented by {@link Tag.Builder} instances when QSL is present.
 */
@InjectedInterface(Tag.Builder.class)
public interface QuiltTagBuilder {
	/**
	 * Clears the contained entries and marks the tag as replaced.
	 *
	 * @return this builder
	 */
	Tag.Builder clearEntries();
}
