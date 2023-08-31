/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.registry.impl.sync.mod_protocol;

import java.util.ArrayList;
import java.util.Map;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public interface ModProtocolContainer {
	Codec<Map<String, IntList>> MAP_CODEC = Codec.unboundedMap(Codec.STRING, Codec.list(Codec.INT).xmap(IntArrayList::new, ArrayList::new));

	static <E> Codec<E> createCodec(Codec<E> codec) {
		return new Codec<>() {
			@Override
			public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> ops, T input) {
				var value = codec.decode(ops, input);
				if (value.get().right().isPresent()) {
					return value;
				}

				ops.get(input, "quilt:mod_protocol").get().ifLeft((x) -> {
					var versionData = MAP_CODEC.decode(ops, x);
					versionData.get().ifLeft(y -> {
						((ModProtocolContainer) (Object) value.result().get().getFirst()).quilt$setModProtocol(y.getFirst());
					});
				});

				return value;
			}

			@Override
			public <T> DataResult<T> encode(E input, DynamicOps<T> ops, T prefix) {
				var value = codec.encode(input, ops, prefix);
				var modProto = ModProtocolContainer.of(input).quilt$getModProtocol();

				if (value.get().left().isPresent() && modProto != null) {
					var x = MAP_CODEC.encodeStart(ops, modProto);

					if (x.get().left().isPresent()) {
						return DataResult.success(ops.set(value.result().get(), "quilt:mod_protocol", x.result().get()));
					}
				}

				return value;
			}
		};
	}

	void quilt$setModProtocol(Map<String, IntList> map);
	Map<String, IntList> quilt$getModProtocol();

	static ModProtocolContainer of(Object object) {
		return (ModProtocolContainer) object;
	}
}
