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

package org.quiltmc.qsl.signal_channel.impl;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Objects;

public sealed interface SubscriptionTracker {
	@NotNull
	Vec3d getPosition();

	boolean shouldRemove();

	record EntityTracker(WeakReference<Entity> entityReference) implements SubscriptionTracker {
		@Override
		@NotNull
		public Vec3d getPosition() {
			//TODO shouldRemove would be called before this
			//     but maybe combine them into one and `return == null => should remove`?
			return Objects.requireNonNull(entityReference.get()).getPos();
		}

		@Override
		public boolean shouldRemove() {
			var entity = entityReference.get();
			return entity == null || entity.isRemoved();
		}
	}

	record BlockEntityTracker(WeakReference<BlockEntity> blockEntityReference) implements SubscriptionTracker {
		@Override
		@NotNull
		public Vec3d getPosition() {
			//TODO see the entity one
			return Vec3d.ofCenter(Objects.requireNonNull(blockEntityReference.get()).getPos());
		}

		@Override
		public boolean shouldRemove() {
			var blockEntity = blockEntityReference.get();
			return blockEntity == null || blockEntity.isRemoved();
		}
	}

	record FixedPositionTracker(Vec3d pos) implements SubscriptionTracker {
		@Override
		@NotNull
		public Vec3d getPosition() {
			return pos;
		}

		@Override
		public boolean shouldRemove() {
			return false;
		}
	}
}
