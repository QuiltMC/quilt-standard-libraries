package org.quiltmc.qsl.key.binds.mixin.client.toggle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.platform.InputUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.option.KeyBind;

import org.quiltmc.qsl.key.binds.api.ToggleableKeyBind;
import org.quiltmc.qsl.key.binds.impl.KeyBindRegistryImpl;

@Mixin(KeyBind.class)
public abstract class KeyBindMixin implements ToggleableKeyBind {
	@Shadow
	@Final
	private static Map<String, KeyBind> KEY_BINDS;

	@Unique
	private static final List<KeyBind> QUILT$ALL_KEY_BINDS = new ArrayList<>();

	@Unique
	private int quilt$disableCounter;

	@Shadow
	public abstract String getTranslationKey();

	@Shadow
	abstract void reset();

	@Inject(method = "<init>(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputUtil$Type;ILjava/lang/String;)V", at = @At("RETURN"))
	private void initializeToggleFields(String string, InputUtil.Type type, int i, String string2, CallbackInfo ci) {
		for (KeyBind otherKey : QUILT$ALL_KEY_BINDS) {
			if (this.equals(otherKey)) {
				throw new IllegalArgumentException(String.format("%s has already been registered!", this.getTranslationKey()));
			} else if (this.getTranslationKey().equals(otherKey.getTranslationKey())) {
				throw new IllegalArgumentException(String.format("Attempted to register {}, but a key bind with the same translation key has already been registered!", this.getTranslationKey()));
			}
		}

		KeyBindRegistryImpl.registerKeyBind((KeyBind) (Object) this);
		quilt$disableCounter = 0;
	}

	@Override
	public boolean isEnabled() {
		return quilt$disableCounter == 0;
	}

	@Override
	public boolean isDisabled() {
		return quilt$disableCounter > 0;
	}

	@Override
	public void enable() {
		// Hahahahaha no.
		if (quilt$disableCounter <= 0) return;

		quilt$disableCounter--;
		if (quilt$disableCounter == 0) {
			KeyBindRegistryImpl.updateKeyBindState((KeyBind) (Object) this);
			KEY_BINDS.put(this.getTranslationKey(), (KeyBind) (Object) this);
			KeyBind.updateBoundKeys();
			this.reset();
		}
	}

	@Override
	public void disable() {
		quilt$disableCounter++;
		if (quilt$disableCounter == 1) {
			KeyBindRegistryImpl.updateKeyBindState((KeyBind) (Object) this);
			KEY_BINDS.remove(this.getTranslationKey(), (KeyBind) (Object) this);
			KeyBind.updateBoundKeys();
			this.reset();
		}
	}
}
