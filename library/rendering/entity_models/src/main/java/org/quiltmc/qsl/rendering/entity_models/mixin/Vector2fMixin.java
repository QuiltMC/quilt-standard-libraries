/*
 * Copyright 2023 QuiltMC
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

package org.quiltmc.qsl.rendering.entity_models.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.util.math.Vector2f;

@Mixin(Vector2f.class)
public abstract class Vector2fMixin {
    @Shadow
    public abstract float getX();

    @Shadow
    public abstract float getY();

    @Override
    public boolean equals(Object o) {
        if (o instanceof Vector2f vec) {
            return this.getX() == vec.getX() && this.getY() == vec.getY();
        }
        return super.equals(o);
    }
}
