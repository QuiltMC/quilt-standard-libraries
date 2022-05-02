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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.mojang.blaze3d.platform.InputUtil;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.JsonOps;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBind;

import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.key.binds.impl.KeyBindRegistryImpl;
import org.quiltmc.qsl.key.binds.impl.chords.KeyChord;
import org.quiltmc.qsl.key.binds.mixin.client.KeyBindAccessor;

// TODO - Massively simplify the config system
public class QuiltKeyBindsConfigManager {
	public static Optional<Boolean> isConfigLoaded = Optional.empty();
	public static final QuiltKeyBindsConfig CONFIG = new QuiltKeyBindsConfig();
	public static final Path QSL_CONFIG_PATH = QuiltLoader.getConfigDir().resolve("qsl");
	public static final Path KEY_BINDS_CONFIG_PATH = QSL_CONFIG_PATH.resolve("key_binds.json");
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

	public static void loadConfig() {
		if (Files.exists(KEY_BINDS_CONFIG_PATH)) {
			try {
				Reader reader = Files.newBufferedReader(KEY_BINDS_CONFIG_PATH, StandardCharsets.UTF_8);
				var result = QuiltKeyBindsConfig.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).result();
				reader.close();

				if (result.isPresent()) {
					registerConfigChanges(result.get());
					populateConfig();
					saveConfig();
					isConfigLoaded = Optional.of(true);
				} else {
					isConfigLoaded = Optional.of(false);
				}
			} catch (IOException | JsonParseException e) {
				System.err.println(e);
			}
		} else {
			if (!Files.isDirectory(QSL_CONFIG_PATH)) {
				try {
					Files.createDirectory(QSL_CONFIG_PATH);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			populateConfig();
			saveConfig();
		}
	}

	// TODO - Wait, why am I using two different config objects? Just use one!
	public static void registerConfigChanges(QuiltKeyBindsConfig newConfig) {
		MinecraftClient client = MinecraftClient.getInstance();
		for (KeyBind key : client.options.allKeys) {
			if (newConfig.getKeyBinds().containsKey(key.getTranslationKey())) {
				Either<String, List<String>> keyEither = newConfig.getKeyBinds().get(key.getTranslationKey());
				keyEither.ifLeft(singleKey -> {
					key.setBoundKey(InputUtil.fromTranslationKey(singleKey));
				});
				keyEither.ifRight(keyList -> {
					switch (keyList.size()) {
					case 0 -> {
						key.setBoundKey(InputUtil.UNKNOWN_KEY);
					}
					case 1 -> {
						key.setBoundKey(InputUtil.fromTranslationKey(keyList.get(0)));
					}
					default -> {
						SortedMap<InputUtil.Key, Boolean> map = new TreeMap<>();
						for (String string : keyList) {
							map.put(InputUtil.fromTranslationKey(string), false);
						}

						key.setBoundChord(new KeyChord(map));
					}
					};
				});
			}
		};
		CONFIG.setShowTutorialToast(newConfig.getShowTutorialToast());
		CONFIG.setKeyBinds(newConfig.getKeyBinds());
		CONFIG.setUnusedKeyBinds(newConfig.getUnusedKeyBinds());
	}

	public static void saveConfig() {
		var result = QuiltKeyBindsConfig.CODEC.encodeStart(JsonOps.INSTANCE, CONFIG).result();
		if (result.isPresent()) {
			try {
				Writer writer = Files.newBufferedWriter(KEY_BINDS_CONFIG_PATH, StandardCharsets.UTF_8);
				JsonWriter jsonWriter = GSON.newJsonWriter(writer);
				GSON.toJson(result.get(), jsonWriter);
				jsonWriter.close();
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}

	public static void populateConfig() {
		Map<String, Either<String, List<String>>> keyBindsMap = new HashMap<>();

		for (KeyBind keyBind : KeyBindRegistryImpl.getAllKeyBinds()) {
			if (keyBind.isDisabled()) continue;
			if (keyBind.getBoundChord() != null) {
				Either<String, List<String>> either = switch (keyBind.getBoundChord().keys.size()) {
				case 0 -> {
					yield Either.right(List.of());
				}
				case 1 -> {
					InputUtil.Key key = keyBind.getBoundChord().keys.firstKey();
					yield Either.left(key.getTranslationKey());
				}
				default -> {
					List<String> list = new ArrayList<>();
					for (InputUtil.Key key : keyBind.getBoundChord().keys.keySet()) {
						list.add(key.getTranslationKey());
					}

					yield Either.right(list);
				} };

				keyBindsMap.put(keyBind.getTranslationKey(), either);
			} else {
				Either<String, List<String>> either =
						((KeyBindAccessor) keyBind).getBoundKey().equals(InputUtil.UNKNOWN_KEY)
						? Either.right(List.of())
						: Either.left(keyBind.getKeyTranslationKey());
				keyBindsMap.put(keyBind.getTranslationKey(), either);
			}
		}

		CONFIG.setKeyBinds(keyBindsMap);
	}
}
