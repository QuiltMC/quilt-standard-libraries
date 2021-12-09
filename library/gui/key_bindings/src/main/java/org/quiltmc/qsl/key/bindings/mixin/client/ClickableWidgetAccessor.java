package org.quiltmc.qsl.key.bindings.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.widget.ClickableWidget;

@Mixin(ClickableWidget.class)
public interface ClickableWidgetAccessor {
	@Invoker
	boolean callClicked(double mouseX, double mouseY);
}
