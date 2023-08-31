/*
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.registry.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.render.item.ItemRenderer;

/**
 * Allows for {@link ItemColors} to be resynchronised with the registry, fixing the bug
 * of the incorrect color renderer being applied to items due to the backing raw identifiers
 * being shifted.
 *
 * @author KJP12
 * @since 3.0.0
 */
@Mixin(ItemRenderer.class)
public interface ItemRendererAccessor {
	@Accessor
	ItemColors getColors();
}
