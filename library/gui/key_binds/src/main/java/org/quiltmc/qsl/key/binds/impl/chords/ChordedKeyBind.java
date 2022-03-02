package org.quiltmc.qsl.key.binds.impl.chords;

public interface ChordedKeyBind {
    KeyChord getBoundChord();
    void setBoundChord(KeyChord chord);
}
