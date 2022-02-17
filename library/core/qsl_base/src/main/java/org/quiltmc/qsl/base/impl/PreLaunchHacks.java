/*
 * Copyright 2016, 2017, 2018, 2019, 2020 zml and Colonel contributors
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

package org.quiltmc.qsl.base.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import org.jetbrains.annotations.ApiStatus;

/**
 * Via i509VCB, a trick to get Brig onto the Knot classpath in order to properly mix in.
 * <p>
 * <b>YOU SHOULD ONLY USE THIS CLASS DURING "preLaunch" and ONLY TARGET A CLASS WHICH IS NOT ANY CLASS YOU MIXIN TO.</b>
 * <p>
 * This will likely not work on Gson because FabricLoader has some special logic related to Gson.
 * <p>
 * Original on GitHub at <a href="https://github.com/i509VCB/Fabric-Junkkyard/blob/ce278daa93804697c745a51af06ec812896ec2ad/src/main/java/me/i509/junkkyard/hacks/PreLaunchHacks.java">i509VCB/Fabric-Junkkyard</a>
 */
@ApiStatus.Experimental
@ApiStatus.Internal
public final class PreLaunchHacks {
	private PreLaunchHacks() {
	}

	private static final ClassLoader KNOT_CLASSLOADER = Thread.currentThread().getContextClassLoader();
	private static final Method ADD_URL_METHOD;

	static {
		try {
			ADD_URL_METHOD = KNOT_CLASSLOADER.getClass().getMethod("addURL", URL.class);
			ADD_URL_METHOD.setAccessible(true);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Failed to load Classloader fields", e);
		}
	}

	/**
	 * Hackily load the package which a mixin may exist within.
	 * <p>
	 * <b>YOU SHOULD NOT TARGET A CLASS WHICH YOU MIXIN TO.</b>
	 *
	 * @param pathOfAClass The path of any class within the package.
	 * @throws ClassNotFoundException    if an unknown class name is used
	 * @throws InvocationTargetException if an error occurs while injecting
	 * @throws IllegalAccessException    if an error occurs while injecting
	 */
	public static void hackilyLoadForMixin(String pathOfAClass) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException {
		URL url = Class.forName(pathOfAClass).getProtectionDomain().getCodeSource().getLocation();
		ADD_URL_METHOD.invoke(KNOT_CLASSLOADER, url);
	}
}
