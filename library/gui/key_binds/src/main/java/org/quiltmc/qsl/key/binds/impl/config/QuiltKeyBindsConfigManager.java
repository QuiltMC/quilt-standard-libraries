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
import com.mojang.serialization.JsonOps;

import org.quiltmc.qsl.key.binds.impl.KeyBindRegistryImpl;
import org.quiltmc.qsl.key.binds.impl.chords.ChordedKeyBind;
import org.quiltmc.qsl.key.binds.impl.chords.KeyChord;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBind;

// TODO - Understand this mess that you have written down
public class QuiltKeyBindsConfigManager {
    public static Optional<Boolean> isConfigLoaded = Optional.empty();
    public static final QuiltKeyBindsConfig CONFIG = new QuiltKeyBindsConfig();
    public static final Path QSL_CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("qsl");
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
                    saveModConfig();
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
            saveModConfig();
        }
    }

    public static void registerConfigChanges(QuiltKeyBindsConfig newConfig) {
        MinecraftClient client = MinecraftClient.getInstance();
        for (KeyBind key : client.options.allKeys) {
            if (newConfig.getKeyBinds().containsKey(key.getTranslationKey())) {
                List<String> keyList = newConfig.getKeyBinds().get(key.getTranslationKey());
                if (keyList.size() == 1) {
                    key.setBoundKey(InputUtil.fromTranslationKey(keyList.get(0)));
                } else if (keyList.size() > 1) {
                    SortedMap<InputUtil.Key, Boolean> map = new TreeMap<>();
                    for (String string : keyList) {
                        map.put(InputUtil.fromTranslationKey(string), false);
                    }
                    ((ChordedKeyBind)key).setBoundChord(new KeyChord(map));
                }
            }
        };
        CONFIG.setKeyBinds(newConfig.getKeyBinds());
    }

    public static void saveModConfig() {
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
        Map<String, List<String>> keyBindMap = new HashMap<>();

        KeyBindRegistryImpl.getAllKeyBinds(true).forEach((keyBind, disabled) -> {
            if (((ChordedKeyBind)keyBind).getBoundChord() != null) {
                List<String> list = new ArrayList<>();
                for (InputUtil.Key key : ((ChordedKeyBind)keyBind).getBoundChord().keys.keySet()) {
                    list.add(key.getTranslationKey());
                }
                keyBindMap.put(keyBind.getTranslationKey(), list);
            } else {
                keyBindMap.put(keyBind.getTranslationKey(), List.of(keyBind.getKeyTranslationKey()));   
            }
            /*
            if (!CONFIG.getKeyBinds().containsKey(key.getTranslationKey())) {
                keyBindMap.put(key.getTranslationKey(), List.of(key.getKeyTranslationKey()));
            } else {
                keyBindMap.put(key.getTranslationKey(), List.of(key.getKeyTranslationKey()));
                //key.setBoundKey(InputUtil.fromTranslationKey(CONFIG.getKeyBinds().get(key.getTranslationKey())));
            }
            */
        });
        CONFIG.setKeyBinds(keyBindMap);
    }
}
