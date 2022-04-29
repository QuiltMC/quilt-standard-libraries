package org.quiltmc.qsl.key.binds.mixin.client.toggle;

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
	private int quilt$disableCounter;

	@Shadow
	public abstract java.lang.String getTranslationKey();

	@Shadow
	abstract void reset();

	@Inject(
			at = @At("RETURN"),
			method = "<init>(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputUtil$Type;ILjava/lang/String;)V"
	)
	private void initializeToggleFields(String string, InputUtil.Type type, int i, String string2, CallbackInfo ci) {
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
			KeyBindRegistryImpl.applyChanges();
			KEY_BINDS.put(this.getTranslationKey(), (KeyBind) (Object) this);
			this.reset();
			KeyBind.updateBoundKeys();
		}
	}

	@Override
	public void disable() {
		if (quilt$disableCounter == 0) {
			KeyBindRegistryImpl.applyChanges();
			KEY_BINDS.remove(this.getTranslationKey(), (KeyBind) (Object) this);
			this.reset();
			KeyBind.updateBoundKeys();
		}

		quilt$disableCounter++;
	}
}
