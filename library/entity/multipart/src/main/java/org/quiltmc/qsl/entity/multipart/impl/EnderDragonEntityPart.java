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

package org.quiltmc.qsl.entity.multipart.impl;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;

import org.quiltmc.qsl.base.api.util.InjectedInterface;
import org.quiltmc.qsl.entity.multipart.api.EntityPart;

@InjectedInterface(EnderDragonPart.class)
public interface EnderDragonEntityPart extends EntityPart<EnderDragonEntity> {
	@Override
	default EnderDragonEntity getOwner() {
		throw new UnsupportedOperationException("No implementation of getOwner could be found.");
	}
}
