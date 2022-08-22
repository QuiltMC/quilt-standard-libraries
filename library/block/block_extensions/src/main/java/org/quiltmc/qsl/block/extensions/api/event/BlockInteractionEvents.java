package org.quiltmc.qsl.block.extensions.api.event;

import org.jetbrains.annotations.NotNull;

import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.EventAwareListener;

public final class BlockInteractionEvents {
	private BlockInteractionEvents() {
		throw new UnsupportedOperationException("BlockInteractionEvents only contains static declarations.");
	}

	public static final Event<IgniteBlock> IGNITE = Event.create(IgniteBlock.class, callbacks -> context -> {
		var result = ActionResult.PASS;
		for (var callback : callbacks) {
			result = callback.onBlockIgnited(context);
			if (result != ActionResult.PASS) {
				return result;
			}
		}
		return result;
	});

	@FunctionalInterface
	public interface IgniteBlock extends EventAwareListener {
		@NotNull ActionResult onBlockIgnited(@NotNull ItemUsageContext context);
	}
}
