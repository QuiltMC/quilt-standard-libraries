package org.quiltmc.qsl.debug_renderers.api;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.util.Identifier;

/**
 * In this class are {@link DebugFeature DebugFeatures} for the vanilla Debug Renderers which do not have other means
 * of activation (i.e., not chunk borders, not collision, and not game test)
 */
public final class VanillaDebugFeatures {
	/**
	 * @see net.minecraft.client.render.debug.DebugRenderer#pathfindingDebugRenderer
	 * @see net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket#DEBUG_PATH
	 */
	public static final DebugFeature PATHFINDING = DebugFeature.register(new Identifier("pathfinding"), true);
	/**
	 * @see net.minecraft.client.render.debug.DebugRenderer#waterDebugRenderer
	 */
	public static final DebugFeature WATER = DebugFeature.register(new Identifier("water"), false);
	/**
	 * @see net.minecraft.client.render.debug.DebugRenderer#heightmapDebugRenderer
	 */
	public static final DebugFeature HEIGHTMAP = DebugFeature.register(new Identifier("heightmap"), false);
	/**
	 * @see net.minecraft.client.render.debug.DebugRenderer#neighborUpdateDebugRenderer
	 * @see net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket#DEBUG_NEIGHBORS_UPDATE
	 */
	public static final DebugFeature NEIGHBORS_UPDATE = DebugFeature.register(new Identifier("neighbors_update"), true);
	/**
	 * @see net.minecraft.client.render.debug.DebugRenderer#structureDebugRenderer
	 * @see net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket#DEBUG_STRUCTURES
	 */
	public static final DebugFeature STRUCTURE = DebugFeature.register(new Identifier("structure"), true);
	/**
	 * @see net.minecraft.client.render.debug.DebugRenderer#skyLightDebugRenderer
	 */
	public static final DebugFeature LIGHT = DebugFeature.register(new Identifier("light"), false);
	/**
	 * @see net.minecraft.client.render.debug.DebugRenderer#worldGenAttemptDebugRenderer
	 * @see net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket#DEBUG_WORLDGEN_ATTEMPT
	 */
	public static final DebugFeature WORLD_GEN_ATTEMPT = DebugFeature.register(new Identifier("world_gen_attempt"), true);
	/**
	 * @see net.minecraft.client.render.debug.DebugRenderer#blockOutlineDebugRenderer
	 */
	public static final DebugFeature SOLID_FACE = DebugFeature.register(new Identifier("solid_face"), false);
	/**
	 * @see net.minecraft.client.render.debug.DebugRenderer#chunkLoadingDebugRenderer
	 */
	public static final DebugFeature CHUNK = DebugFeature.register(new Identifier("chunk"), false);
	/**
	 * @see net.minecraft.client.render.debug.DebugRenderer#villageDebugRenderer
	 * @see net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket#DEBUG_POI_ADDED
	 * @see net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket#DEBUG_POI_REMOVED
	 * @see net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket#DEBUG_POI_TICKET_COUNT
	 * @see net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket#DEBUG_BRAIN
	 */
	public static final DebugFeature BRAIN = DebugFeature.register(new Identifier("brain"), true);
	/**
	 * @see net.minecraft.client.render.debug.DebugRenderer#villageSectionsDebugRenderer
	 * @see net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket#DEBUG_VILLAGE_SECTIONS
	 */
	public static final DebugFeature VILLAGE_SECTIONS = DebugFeature.register(new Identifier("village_sections"), true);
	/**
	 * @see net.minecraft.client.render.debug.DebugRenderer#beeDebugRenderer
	 * @see net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket#DEBUG_BEE
	 * @see net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket#DEBUG_HIVE
	 */
	public static final DebugFeature BEE = DebugFeature.register(new Identifier("bee"), true);
	/**
	 * @see net.minecraft.client.render.debug.DebugRenderer#raidCenterDebugRenderer
	 * @see net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket#DEBUG_RAIDS
	 */
	public static final DebugFeature RAID = DebugFeature.register(new Identifier("raid"), true);
	/**
	 * @see net.minecraft.client.render.debug.DebugRenderer#goalSelectorDebugRenderer
	 * @see net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket#DEBUG_GOAL_SELECTOR
	 */
	public static final DebugFeature GOAL_SELECTOR = DebugFeature.register(new Identifier("goal_selector"), true);
	/**
	 * @see net.minecraft.client.render.debug.DebugRenderer#gameEventDebugRenderer
	 * @see net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket#DEBUG_GAME_EVENT
	 * @see net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket#DEBUG_GAME_EVENT_LISTENERS
	 */
	public static final DebugFeature GAME_EVENT = DebugFeature.register(new Identifier("game_event"), true);

	private VanillaDebugFeatures() {}

	@ApiStatus.Internal
	public static void init() {}
}
