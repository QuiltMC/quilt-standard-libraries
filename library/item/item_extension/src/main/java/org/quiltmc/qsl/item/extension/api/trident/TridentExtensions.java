package org.quiltmc.qsl.item.extension.api.trident;

import org.quiltmc.qsl.base.api.util.InjectedInterface;

import net.minecraft.item.TridentItem;
import net.minecraft.util.Identifier;

@InjectedInterface(TridentItem.class)
public interface TridentExtensions {
    boolean useVanillaRenderer();
    Identifier getRenderTexture();

}
