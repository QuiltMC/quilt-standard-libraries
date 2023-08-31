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

package org.quiltmc.qsl.data.callback.api;

import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;

/**
 * Marks an object that may be aware of a codec that can be used to encode it. Implementing objects should override
 * {@link #getCodecId()} to return the identifier of the codec if they are encodable, or leave the default
 * implementation if they are not. Identifiers provided should correspond to the identifiers of codecs registered in
 * some {@link CodecMap}.
 */
public interface CodecAware {
	/**
	 * {@return the identifier of the codec that can be used to encode this object, or {@code null} if this object is not encodable}
	 */
	default @Nullable Identifier getCodecId() {
		return null;
	}
}
