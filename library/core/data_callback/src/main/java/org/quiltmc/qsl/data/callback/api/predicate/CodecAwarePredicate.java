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

package org.quiltmc.qsl.data.callback.api.predicate;

import java.util.function.Predicate;

import org.quiltmc.qsl.data.callback.api.CodecAware;

/**
 * A predicate which may be aware of a codec that can be used to encode it.
 *
 * @param <T> the type of the input to the test
 */
public interface CodecAwarePredicate<T> extends CodecAware, Predicate<T> {}
