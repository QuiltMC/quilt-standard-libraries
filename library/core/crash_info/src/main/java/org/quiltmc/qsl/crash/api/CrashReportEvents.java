package org.quiltmc.qsl.crash.api;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.base.api.event.ArrayEvent;

public final class CrashReportEvents {
	public static final ArrayEvent<SystemDetails> SYSTEM_DETAILS = ArrayEvent.create(SystemDetails.class, callbacks -> details -> {
		for (var callback : callbacks) {
			callback.addDetails(details);
		}
	});

	public static final ArrayEvent<WorldDetails> WORLD_DETAILS = ArrayEvent.create(WorldDetails.class, callbacks -> (world, section) -> {
		for (var callback : callbacks) {
			callback.addDetails(world, section);
		}
	});

	public static final ArrayEvent<BlockDetails> BLOCK_DETAILS = ArrayEvent.create(BlockDetails.class, callbacks -> (world, pos, state, section) -> {
		for (var callback : callbacks) {
			callback.addDetails(world, pos, state, section);
		}
	});

	public static final ArrayEvent<EntityDetails> ENTITY_DETAILS = ArrayEvent.create(EntityDetails.class, callbacks -> (entity, section) -> {
		for (var callback : callbacks) {
			callback.addDetails(entity, section);
		}
	});

	public static final ArrayEvent<BlockEntityDetails> BLOCKENTITY_DETAILS = ArrayEvent.create(BlockEntityDetails.class, callbacks -> (blockentity, section) -> {
		for (var callback : callbacks) {
			callback.addDetails(blockentity, section);
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
	public interface BlockDetails {
		void addDetails(HeightLimitView world, BlockPos pos, @Nullable BlockState state, CrashReportSection section);
	}

	@FunctionalInterface
	public interface EntityDetails {
		void addDetails(Entity entity, CrashReportSection section);
	}

	@FunctionalInterface
	public interface BlockEntityDetails {
		void addDetails(BlockEntity entity, CrashReportSection section);
	}

	@FunctionalInterface
	public interface CrashReportCreation {
		void onCreate(CrashReport report);
	}
}
