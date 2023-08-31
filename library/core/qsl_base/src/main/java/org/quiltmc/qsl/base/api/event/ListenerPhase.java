/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.base.api.event;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.minecraft.util.Identifier;

/**
 * Annotates a specific callback in a listener a specific phase to listen.
 *
 * @see Event#addPhaseOrdering(Identifier, Identifier)
 * @see Event#register(Identifier, Object)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ListenerPhase {
	/**
	 * {@return the targeted callback interface}
	 */
	Class<?> callbackTarget();

	/**
	 * {@return the namespace of the phase to listen}
	 */
	String namespace();

	/**
	 * {@return the path of the phase to listen}
	 */
	String path();
}
