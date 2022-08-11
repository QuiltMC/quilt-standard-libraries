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

package org.quiltmc.qsl.registry.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.util.collection.IdList;
import org.quiltmc.qsl.registry.impl.sync.SynchronizedIdList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(IdList.class)
public class IdListMixin<T> implements SynchronizedIdList<T> {
	@Shadow
	private int nextId;

	@Shadow
	@Final
	private List<T> list;

	@Shadow
	@Final
	private Object2IntMap<T> idMap;

	@Override
	public void quilt$clear() {
		this.nextId = 0;
		this.list.clear();
		this.idMap.clear();
	}
}
