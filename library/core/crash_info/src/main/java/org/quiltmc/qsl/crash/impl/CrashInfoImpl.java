package org.quiltmc.qsl.crash.impl;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.crash.api.CrashReportEvents;

@ApiStatus.Internal
public final class CrashInfoImpl implements ModInitializer {
	@Override
	public void onInitialize() {
		CrashReportEvents.SYSTEM_DETAILS.register(details -> details.addSection("Quilt Mods", () -> {
			StringBuilder builder = new StringBuilder();

			for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
				var metadata = mod.getMetadata();

				builder.append("\n\t\t%s: %s %s".formatted(
						metadata.getId(),
						metadata.getName(),
						metadata.getVersion().getFriendlyString())
				);
			}

			return builder.toString();
		}));
	}
}
