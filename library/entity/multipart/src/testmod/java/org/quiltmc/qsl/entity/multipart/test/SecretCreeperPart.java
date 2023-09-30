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

package org.quiltmc.qsl.entity.multipart.test;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.math.Vec3d;

import org.quiltmc.qsl.entity.multipart.api.AbstractEntityPart;

public class SecretCreeperPart extends AbstractEntityPart<CreeperEntity> {
	public SecretCreeperPart(CreeperEntity creeper, float width, float height, Vec3d relativePosition, Vec3d relativePivot) {
		super(creeper, width, height);
		this.setRelativePosition(relativePosition);
		this.setPivot(relativePivot);
	}

	public boolean damage(DamageSource source, float amount) {
		return super.damage(source, amount * 10);
	}
}
