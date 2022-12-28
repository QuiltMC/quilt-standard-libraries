package org.quiltmc.qsl.debug_renderers.mixin;

import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.piece.StructurePiece;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.debug_renderers.api.VanillaDebugFeatures;
import org.quiltmc.qsl.debug_renderers.impl.Initializer;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Objects;

@Mixin(DebugInfoSender.class)
public class DebugInfoSenderMixin {
	//TODO re-implement the empty methods in DebugInfoSender
	/**
	 * @author QuiltMC, Will BL
	 * @reason Re-implementation of method with missing body
	 */
	@Overwrite
	public static void sendPathfindingData(World world, MobEntity mob, @Nullable Path path, float nodeReachProximity) {
		if (path == null || world.isClient() || !Initializer.HAS_NETWORKING) {
			return;
		}
		var buf = PacketByteBufs.create();
		buf.writeInt(mob.getId());
		path.toBuffer(buf);
		buf.writeFloat(nodeReachProximity);
		ServerPlayNetworking.send(
				VanillaDebugFeatures.PATHFINDING.getPlayersWithFeatureEnabled(world.getServer()),
				CustomPayloadS2CPacket.DEBUG_PATH,
				buf
		);
	}

	/**
	 * @author QuiltMC, Will BL
	 * @reason Re-implementation of method with missing body
	 */
	@Overwrite
	public static void sendNeighborUpdate(World world, BlockPos pos) {
		if (world.isClient() || !Initializer.HAS_NETWORKING) {
			return;
		}

		var buf = PacketByteBufs.create();
		buf.writeVarLong(world.getTime());
		buf.writeBlockPos(pos);
		ServerPlayNetworking.send(
				VanillaDebugFeatures.NEIGHBORS_UPDATE.getPlayersWithFeatureEnabled(world.getServer()),
				CustomPayloadS2CPacket.DEBUG_NEIGHBORS_UPDATE,
				buf
		);
	}

	/**
	 * @author QuiltMC, Will BL
	 * @reason Re-implementation of method with missing body
	 */
	@Overwrite
	public static void sendStructureStart(StructureWorldAccess world, StructureStart structureStart) {
		if (world.isClient() || !Initializer.HAS_NETWORKING) {
			return;
		}
		var server = Objects.requireNonNull(world.getServer());
		var registryManager = server.getRegistryManager();
		var buf = PacketByteBufs.create();
		buf.writeIdentifier(registryManager.get(Registry.DIMENSION_TYPE_KEY).getId(world.getDimension()));
		var box = structureStart.setBoundingBoxFromChildren();
		buf.writeInt(box.getMinX());
		buf.writeInt(box.getMinY());
		buf.writeInt(box.getMinZ());
		buf.writeInt(box.getMaxX());
		buf.writeInt(box.getMaxY());
		buf.writeInt(box.getMaxZ());
		var children = structureStart.getChildren();
		buf.writeInt(children.size());
		for (int i = 0; i < children.size(); i++) {
			StructurePiece child = children.get(i);
			box = child.getBoundingBox();
			buf.writeInt(box.getMinX());
			buf.writeInt(box.getMinY());
			buf.writeInt(box.getMinZ());
			buf.writeInt(box.getMaxX());
			buf.writeInt(box.getMaxY());
			buf.writeInt(box.getMaxZ());
			buf.writeBoolean(i == 0);
		}
		ServerPlayNetworking.send(
				VanillaDebugFeatures.STRUCTURE.getPlayersWithFeatureEnabled(world.getServer()),
				CustomPayloadS2CPacket.DEBUG_STRUCTURES,
				buf
		);
	}

}
