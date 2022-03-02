package org.quiltmc.qsl.key.binds.impl.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class QuiltKeyBindsConfig {
    public static final Codec<QuiltKeyBindsConfig> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            // TODO - Implement me!
            Codec.BOOL.fieldOf("show_tutorial_toast").forGetter(QuiltKeyBindsConfig::getShowTutorialToast),
            // TODO - Ideally, it would be a list for chords, a single string for single keys, and an empty list for unbound 
            Codec.unboundedMap(Codec.STRING, Codec.list(Codec.STRING)).fieldOf("key_binds").forGetter(QuiltKeyBindsConfig::getKeyBinds)
        )
        .apply(instance, QuiltKeyBindsConfig::new)
    );

    private boolean showTutorialToast;
    private Map<String, List<String>> keyBinds;

    public QuiltKeyBindsConfig() {
        this.keyBinds = new HashMap<>();
        this.showTutorialToast = false;
    }

    public QuiltKeyBindsConfig(
        boolean showTutorialToast,
        Map<String, List<String>> keyBinds
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

    public Map<String, List<String>> getKeyBinds() {
        return keyBinds;
    }

    public void setKeyBinds(Map<String, List<String>> keyBinds) {
        this.keyBinds = keyBinds;
    }
}
