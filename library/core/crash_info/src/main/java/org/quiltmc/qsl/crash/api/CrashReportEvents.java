package org.quiltmc.qsl.crash.api;

import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.world.World;
import org.quiltmc.qsl.base.api.event.ArrayEvent;

public final class CrashReportEvents {
	public static final ArrayEvent<SystemDetails> SYSTEM_DETAILS = ArrayEvent.create(SystemDetails.class, callbacks -> details -> {
		for (var callback : callbacks) {
			callback.addDetails(details);
		}
	});

	public static final ArrayEvent<WorldDetails> WORLD_DETAILS = ArrayEvent.create(WorldDetails.class, callbacks -> (world, details) -> {
		for (var callback : callbacks) {
			callback.addDetails(world, details);
		}
	});

	public static final ArrayEvent<CrashReportCreation> CRASH_REPORT_CREATED = ArrayEvent.create(CrashReportCreation.class, callbacks -> report -> {
		for (var callback : callbacks) {
			callback.onCreate(report);
		}
	});


	@FunctionalInterface
	public interface SystemDetails {
		void addDetails(net.minecraft.util.SystemDetails details);
	}

	@FunctionalInterface
	public interface WorldDetails {
		void addDetails(World world, CrashReportSection section);
	}

	@FunctionalInterface
	public interface CrashReportCreation {
		void onCreate(CrashReport report);
	}
}
