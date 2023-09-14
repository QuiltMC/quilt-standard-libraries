/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.testing.impl.game.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.data.DataWriter;
import net.minecraft.data.dev.NbtProvider;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.StructureTestUtil;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@ApiStatus.Internal
public final class QuiltTestCommand {
	public static int executeExport(ServerCommandSource source) {
		BlockPos currentPos = BlockPos.fromPosition(source.getPosition());
		ServerWorld world = source.getWorld();
		BlockPos nearestStructureBlockPos = StructureTestUtil.findNearestStructureBlock(currentPos, 15, world);

		if (nearestStructureBlockPos == null) {
			source.sendError(Text.literal("Couldn't find any structure block within 15 blocks radius."));
			return 0;
		} else {
			var structureBlock = (StructureBlockBlockEntity) world.getBlockEntity(nearestStructureBlockPos);
			return executeExport(source, structureBlock.getStructureName());
		}
	}

	public static int executeExport(ServerCommandSource source, String structure) {
		Path directoryPath = Paths.get(StructureTestUtil.testStructuresDirectoryName);
		var structureId = new Identifier(structure);

		Path structurePath = source.getWorld().getStructureTemplateManager().getStructurePath(structureId, ".nbt");
		Path exportedPath = NbtProvider.convertNbtToSnbt(DataWriter.UNCACHED, structurePath, structure.replace(':', '/'), directoryPath);

		if (exportedPath == null) {
			source.sendError(Text.literal("Failed to export " + structurePath));
			source.sendError(Text.literal("Hint: you need to save the structure first with the structure block."));
			return 1;
		} else {
			try {
				Files.createDirectories(exportedPath.getParent());
			} catch (IOException error) {
				source.sendError(Text.literal("Could not create folder " + exportedPath.getParent() + ", a stack trace is available in the logs."));
				error.printStackTrace();
				return 1;
			}

			source.sendSystemMessage(Text.literal("Exported ")
					.append(Text.literal(structure).formatted(Formatting.GOLD))
					.append(" to ")
					.append(Text.literal(exportedPath.toAbsolutePath().toString()).formatted(Formatting.GOLD))
					.append(" [")
					.append(Text.literal("Open Directory")
							.styled(style -> style.withColor(Formatting.GREEN)
									.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, directoryPath.toAbsolutePath().toString()))
									.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to open.")))
							)
					)
					.append("]")
			);
			return 0;
		}
	}
}
