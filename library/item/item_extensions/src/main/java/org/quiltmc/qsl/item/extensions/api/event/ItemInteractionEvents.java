package org.quiltmc.qsl.item.extensions.api.event;

import org.jetbrains.annotations.NotNull;

import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.EventAwareListener;

public final class ItemInteractionEvents {
	private ItemInteractionEvents() {
		throw new UnsupportedOperationException("BlockInteractionEvents only contains static declarations.");
	}

	public static final Event<IgniteBlock> IGNITE_BLOCK = Event.create(IgniteBlock.class, callbacks -> context -> {
		var result = ActionResult.PASS;
		for (var callback : callbacks) {
			result = callback.onIgniteBlock(context);
			if (result != ActionResult.PASS) {
				return result;
			}
		}
		return result;
	});

	@FunctionalInterface
	public interface IgniteBlock extends EventAwareListener {
		@NotNull ActionResult onIgniteBlock(@NotNull ItemUsageContext context);
	}
}
