/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.crash.api;

import java.nio.file.Path;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.EventAwareListener;

/**
 * Events which allow the manipulation of crash reports.
 *
 * <p>Crash reports are created on the client and the server during a crash, and are also produced on the server every
 * tick if debug/performance recording is enabled. ({@link net.minecraft.server.world.ServerWorld#dump(Path)})</p>
 */
public final class CrashReportEvents {
	/**
	 * An event for adding information to the "System Details" section of the crash report.
	 *
	 * <p>This section is added to all crashes.
	 */
	public static final Event<SystemDetails> SYSTEM_DETAILS = Event.create(SystemDetails.class, callbacks -> details -> {
		for (var callback : callbacks) {
			callback.addDetails(details);
		}
	});

	/**
	 * An event for adding information to the "Affected level" section of the crash report.
	 *
	 * <p>This section is added to all crashes caused by something in-world.
	 */
	public static final Event<WorldDetails> WORLD_DETAILS = Event.create(WorldDetails.class, callbacks -> (world, section) -> {
		for (var callback : callbacks) {
			callback.addDetails(world, section);
		}
	});

	/**
	 * An event for adding information about a block to a crash report.
	 *
	 * <p>This is used as its own section if a block tick causes the crash, or is appended to a block entity section if
	 * a block entity causes a crash.
	 */
	public static final Event<BlockDetails> BLOCK_DETAILS = Event.create(BlockDetails.class, callbacks -> (world, pos, state, section) -> {
		for (var callback : callbacks) {
			callback.addDetails(world, pos, state, section);
		}
	});

	/**
	 * An event for adding information about an entity to a crash report.
	 *
	 * <p>This section is added to crashes caused by ticking or rendering an entity.
	 */
	public static final Event<EntityDetails> ENTITY_DETAILS = Event.create(EntityDetails.class, callbacks -> (entity, section) -> {
		for (var callback : callbacks) {
			callback.addDetails(entity, section);
		}
	});

	/**
	 * An event for adding information about a block entity to a crash report.
	 *
	 * <p>This section is added to crashes caused by ticking or rendering a block entity.
	 */
	public static final Event<BlockEntityDetails> BLOCKENTITY_DETAILS = Event.create(BlockEntityDetails.class, callbacks -> (blockEntity, section) -> {
		for (var callback : callbacks) {
			callback.addDetails(blockEntity, section);
		}
	});

	/**
	 * An event for modifying a crash report before it is {@link CrashReport#addStackTrace(StringBuilder) stringified}.
	 *
	 * <p>This can be used for adding new sections, with {@link CrashReport#addElement(String)}.
	 */
	public static final Event<CrashReportCreation> CRASH_REPORT_CREATION = Event.create(CrashReportCreation.class, callbacks -> report -> {
		for (var callback : callbacks) {
			callback.onCreate(report);
		}
	});

	/**
	 * Functional interface to be implemented on callbacks for {@link #SYSTEM_DETAILS}.
	 *
	 * @see #SYSTEM_DETAILS
	 */
	@FunctionalInterface
	public interface SystemDetails extends EventAwareListener {
		void addDetails(net.minecraft.util.SystemDetails details);
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #WORLD_DETAILS}.
	 *
	 * @see #WORLD_DETAILS
	 */
	@FunctionalInterface
	public interface WorldDetails extends EventAwareListener {
		void addDetails(World world, CrashReportSection section);
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #BLOCK_DETAILS}.
	 *
	 * @see #BLOCK_DETAILS
	 */
	@FunctionalInterface
	public interface BlockDetails extends EventAwareListener {
		void addDetails(HeightLimitView world, BlockPos pos, @Nullable BlockState state, CrashReportSection section);
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #ENTITY_DETAILS}.
	 *
	 * @see #ENTITY_DETAILS
	 */
	@FunctionalInterface
	public interface EntityDetails extends EventAwareListener {
		void addDetails(Entity entity, CrashReportSection section);
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #BLOCKENTITY_DETAILS}.
	 *
	 * @see #BLOCKENTITY_DETAILS
	 */
	@FunctionalInterface
	public interface BlockEntityDetails extends EventAwareListener {
		void addDetails(BlockEntity entity, CrashReportSection section);
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #CRASH_REPORT_CREATION}.
	 *
	 * @see #CRASH_REPORT_CREATION
	 */
	@FunctionalInterface
	public interface CrashReportCreation extends EventAwareListener {
		void onCreate(CrashReport report);
	}
}
