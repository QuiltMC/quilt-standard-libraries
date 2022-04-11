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

package org.quiltmc.qsl.key.binds.impl.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class QuiltKeyBindsConfig {
	public static final Codec<QuiltKeyBindsConfig> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
				// TODO - Implement me!
				Codec.BOOL.fieldOf("show_tutorial_toast").forGetter(QuiltKeyBindsConfig::getShowTutorialToast),
				Codec.unboundedMap(Codec.STRING, Codec.either(Codec.STRING, Codec.list(Codec.STRING))).fieldOf("key_binds").forGetter(QuiltKeyBindsConfig::getKeyBinds)
			)
			.apply(instance, QuiltKeyBindsConfig::new)
	);

	private boolean showTutorialToast;
	// TODO - This type is super long; Use an object for this
	private Map<String, Either<String, List<String>>> keyBinds;

	public QuiltKeyBindsConfig() {
		this.keyBinds = new HashMap<>();
		this.showTutorialToast = false;
	}

	public QuiltKeyBindsConfig(
			boolean showTutorialToast,
			Map<String, Either<String, List<String>>> keyBinds
	) {
		this.showTutorialToast = showTutorialToast;
		this.keyBinds = keyBinds;
	}

	public boolean getShowTutorialToast() {
		return this.showTutorialToast;
	}

	public void setShowTutorialToast(boolean showTutorialToast) {
		this.showTutorialToast = showTutorialToast;
	}

	public Map<String, Either<String, List<String>>> getKeyBinds() {
		return keyBinds;
	}

	public void setKeyBinds(Map<String, Either<String, List<String>>> keyBinds) {
		this.keyBinds = keyBinds;
	}
}
