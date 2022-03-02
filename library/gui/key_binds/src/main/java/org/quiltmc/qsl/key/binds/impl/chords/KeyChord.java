package org.quiltmc.qsl.key.binds.impl.chords;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.platform.InputUtil;

public class KeyChord {
    // TODO - Private this, add methods for getting/modifying it
    public Map<InputUtil.Key, Boolean> keys = new HashMap<>();

    public KeyChord(Map<InputUtil.Key, Boolean> keys) {
        this.keys = keys;
    }
    
    public KeyChord() {}
}
