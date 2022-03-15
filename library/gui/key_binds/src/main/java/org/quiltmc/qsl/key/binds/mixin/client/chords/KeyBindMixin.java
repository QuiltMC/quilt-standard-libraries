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

package org.quiltmc.qsl.key.binds.mixin.client.chords;

import java.util.Iterator;
import java.util.Map;

import com.mojang.blaze3d.platform.InputUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBind;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import org.quiltmc.qsl.key.binds.impl.chords.ChordedKeyBind;
import org.quiltmc.qsl.key.binds.impl.chords.KeyChord;

@Mixin(KeyBind.class)
public class KeyBindMixin implements ChordedKeyBind {
	@Shadow
	@Final
	private static Map<String, KeyBind> KEY_BINDS;

	@Shadow
	private InputUtil.Key boundKey;

	@Unique
	private static final Map<KeyChord, KeyBind> KEY_BINDS_BY_CHORD = new Reference2ReferenceOpenHashMap<>();

	@Unique
	@Final
	private KeyChord quilt$defaultChord;

	@Unique
	private KeyChord quilt$boundChord;

	@Inject(
			at = @At("RETURN"),
			method = "<init>(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputUtil$Type;ILjava/lang/String;)V"
	)
	private void expandInit(String string, InputUtil.Type type, int i, String string2, CallbackInfo ci) {
		quilt$defaultChord = null;
		quilt$boundChord = null;
	}

	@Inject(at = @At("HEAD"), method = "onKeyPressed")
	private static void detectChordsOnIncrement(InputUtil.Key startingKey, CallbackInfo ci) {
		for (KeyChord chord : KEY_BINDS_BY_CHORD.keySet()) {
			if (chord.keys.containsKey(startingKey) && !chord.keys.containsValue(false)) {
				// This ensures that the chord will only be incremented once instead of N times
				if (startingKey.equals(chord.keys.keySet().toArray()[0])) {
					KeyBind keyBind = KEY_BINDS_BY_CHORD.get(chord);
					((KeyBindAccessor) keyBind).setTimesPressed(((KeyBindAccessor) keyBind).getTimesPressed() + 1);
				}
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "setKeyPressed")
	private static void detectChordsOnSet(InputUtil.Key startingKey, boolean pressed, CallbackInfo ci) {
		for (KeyChord chord : KEY_BINDS_BY_CHORD.keySet()) {
			if (chord.keys.containsKey(startingKey)) {
				chord.keys.put(startingKey, pressed);

				if (!chord.keys.containsValue(false)) {
					KEY_BINDS_BY_CHORD.get(chord).setPressed(true);
				} else {
					KEY_BINDS_BY_CHORD.get(chord).setPressed(false);
				}
			}
		}
	}

	@Inject(
			at = @At(
				value = "INVOKE",
				target = "Lcom/mojang/blaze3d/platform/InputUtil$Key;getType()Lcom/mojang/blaze3d/platform/InputUtil$Type;"
			),
			method = "updatePressedStates",
			locals = LocalCapture.CAPTURE_FAILHARD,
			cancellable = true
	)
	private static void updateChordsToo(CallbackInfo ci, Iterator<?> iterator, KeyBind keyBind) {
		KeyChord chord = ((KeyBindMixin) (Object) keyBind).quilt$boundChord;
		if (chord != null) {
			long window = MinecraftClient.getInstance().getWindow().getHandle();
			for (InputUtil.Key key : chord.keys.keySet()) {
				if (key.getType() == InputUtil.Type.KEYSYM) {
					chord.keys.put(key, InputUtil.isKeyPressed(window, key.getKeyCode()));
				}
			}

			// TODO - Create an "Update Chord" method for this
			if (!chord.keys.containsValue(false)) {
				KEY_BINDS_BY_CHORD.get(chord).setPressed(true);
			} else {
				KEY_BINDS_BY_CHORD.get(chord).setPressed(false);
			}

			ci.cancel();
		}
	}

	@Inject(at = @At("HEAD"), method = "updateBoundKeys")
	private static void updateChordBoundKeys(CallbackInfo cir) {
		KEY_BINDS_BY_CHORD.clear();

		for (KeyBind key : KEY_BINDS.values()) {
			KeyChord chord = ((KeyBindMixin) (Object) key).quilt$boundChord;
			if (chord != null) {
				KEY_BINDS_BY_CHORD.put(chord, key);
			}
		}
	}

	// TODO - Detect chords for matchesKey too; They are such a weird case
	@Inject(at = @At("HEAD"), method = "matchesKey", cancellable = true)
	private void matchesChordKey(CallbackInfoReturnable<Boolean> ci) { }

	// TODO - Detect chords for matchesMouseButton as well

	@Inject(at = @At("HEAD"), method = "setBoundKey", cancellable = true)
	private void resetChord(CallbackInfo ci) {
		this.quilt$boundChord = null;
	}

	@Inject(at = @At("HEAD"), method = "keyEquals", cancellable = true)
	private void keyOrChordEquals(KeyBind other, CallbackInfoReturnable<Boolean> cir) {
		if (this.quilt$boundChord != null) {
			if (((ChordedKeyBind) other).getBoundChord() != null) {
				cir.setReturnValue(this.quilt$boundChord.equals(((ChordedKeyBind) other).getBoundChord()));
			} else {
				cir.setReturnValue(false);
			}
		}
	}

	@Inject(at = @At("RETURN"), method = "isUnbound", cancellable = true)
	private void isChordUnbound(CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ() && this.quilt$boundChord != null) {
			cir.setReturnValue(false);
		}
	}

	@Inject(at = @At("HEAD"), method = "getKeyName", cancellable = true)
	private void useChordName(CallbackInfoReturnable<Text> cir) {
		if (this.quilt$boundChord != null) {
			MutableText text = LiteralText.EMPTY.shallowCopy();
			for (InputUtil.Key key : this.quilt$boundChord.keys.keySet()) {
				if (text.getSiblings().size() != 0) {
					text.append(" + ");
				}

				text.append(key.getDisplayText());
			}

			cir.setReturnValue(text);
		}
	}

	@Inject(at = @At("HEAD"), method = "isDefault", cancellable = true)
	private void detectDefaultChord(CallbackInfoReturnable<Boolean> cir) {
		if (this.quilt$boundChord != null) {
			cir.setReturnValue(this.quilt$boundChord.equals(this.quilt$defaultChord));
		}
	}

	@Override
	public KeyChord getBoundChord() {
		return this.quilt$boundChord;
	}

	@Override
	public void setBoundChord(KeyChord chord) {
		this.quilt$boundChord = chord;
		this.boundKey = InputUtil.UNKNOWN_KEY;
	}
}
