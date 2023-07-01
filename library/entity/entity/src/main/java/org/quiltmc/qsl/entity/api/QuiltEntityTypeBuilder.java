/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.entity.api;

import java.util.Objects;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.feature_flags.FeatureFlag;
import net.minecraft.feature_flags.FeatureFlagBitSet;
import net.minecraft.feature_flags.FeatureFlags;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import org.quiltmc.qsl.entity.impl.QuiltEntityType;

/**
 * Extended version of {@link EntityType.Builder} with added registration for
 * server-&gt;client entity tracking values.
 *
 * @param <T> Entity class
 */
public class QuiltEntityTypeBuilder<T extends Entity> {
	private EntityType.EntityFactory<T> factory;
	private @NotNull SpawnGroup spawnGroup;
	private ImmutableSet<Block> canSpawnInside = ImmutableSet.of();
	private boolean saveable = true;
	private boolean summonable = true;
	private boolean fireImmune = false;
	private boolean spawnableFarFromPlayer;
	private int maxTrackingRange = 5;
	private int trackingTickInterval = 3;
	private FeatureFlagBitSet requiredFlags = FeatureFlags.DEFAULT_SET;
	private Boolean alwaysUpdateVelocity = null;
	private EntityDimensions dimensions = EntityDimensions.changing(0.6F, 1.8F);

	protected QuiltEntityTypeBuilder(@NotNull SpawnGroup spawnGroup, @NotNull EntityType.EntityFactory<T> factory) {
		this.spawnGroup = spawnGroup;
		this.factory = factory;
		this.spawnableFarFromPlayer = spawnGroup == SpawnGroup.CREATURE || spawnGroup == SpawnGroup.MISC;
	}

	/**
	 * Creates an entity type builder.
	 * <p>
	 * This entity's spawn group will automatically be set to {@link SpawnGroup#MISC}.
	 *
	 * @param <T> the type of entity
	 * @return a new entity type builder
	 */
	public static <T extends Entity> QuiltEntityTypeBuilder<T> create() {
		return create(SpawnGroup.MISC);
	}

	/**
	 * Creates an entity type builder.
	 *
	 * @param spawnGroup the entity spawn group
	 * @param <T> the type of entity
	 * @return a new entity type builder
	 */
	public static <T extends Entity> QuiltEntityTypeBuilder<T> create(@NotNull SpawnGroup spawnGroup) {
		return create(spawnGroup, QuiltEntityTypeBuilder::emptyFactory);
	}

	/**
	 * Creates an entity type builder.
	 *
	 * @param spawnGroup the entity spawn group
	 * @param factory the entity factory used to create this entity
	 * @param <T> the type of entity
	 * @return a new entity type builder
	 */
	public static <T extends Entity> QuiltEntityTypeBuilder<T> create(@NotNull SpawnGroup spawnGroup, @NotNull EntityType.EntityFactory<T> factory) {
		return new QuiltEntityTypeBuilder<>(spawnGroup, factory);
	}

	/**
	 * Creates an entity type builder for a living entity.
	 * <p>
	 * This entity's spawn group will automatically be set to {@link SpawnGroup#MISC}.
	 *
	 * @param <T> the type of entity
	 * @return a new living entity type builder
	 */
	public static <T extends LivingEntity> QuiltEntityTypeBuilder.Living<T> createLiving() {
		return new QuiltEntityTypeBuilder.Living<>(SpawnGroup.MISC, QuiltEntityTypeBuilder::emptyFactory);
	}

	/**
	 * Creates an entity type builder for a mob entity.
	 *
	 * @param <T> the type of entity
	 * @return a new mob entity type builder
	 */
	public static <T extends MobEntity> QuiltEntityTypeBuilder.Mob<T> createMob() {
		return new QuiltEntityTypeBuilder.Mob<>(SpawnGroup.MISC, QuiltEntityTypeBuilder::emptyFactory);
	}

	/**
	 * A placeholder factory for new entity type builders.
	 *
	 * @param <T> the type of entity
	 * @param type the entity type
	 * @param world the world
	 * @return null
	 */
	@Contract("_,_->null")
	private static <T extends Entity> T emptyFactory(EntityType<T> type, World world) {
		return null;
	}

	/**
	 * Sets the spawn group for this entity type.
	 *
	 * @param group the spawn group
	 * @return this builder for chaining
	 */
	public QuiltEntityTypeBuilder<T> spawnGroup(@NotNull SpawnGroup group) {
		Objects.requireNonNull(group, "Spawn group cannot be null");
		this.spawnGroup = group;
		return this;
	}

	/**
	 * Sets the entity factory for this entity type.
	 *
	 * @param <N> the type of entity
	 * @param factory the entity factory
	 * @return this builder for chaining
	 */
	@SuppressWarnings("unchecked")
	public <N extends T> QuiltEntityTypeBuilder<N> entityFactory(@NotNull EntityType.EntityFactory<N> factory) {
		Objects.requireNonNull(factory, "Entity Factory cannot be null");
		this.factory = (EntityType.EntityFactory<T>) factory;
		return (QuiltEntityTypeBuilder<N>) this;
	}

	/**
	 * Sets this entity type to not be summonable using the {@code /summon} command.
	 *
	 * @return this builder for chaining
	 */
	public QuiltEntityTypeBuilder<T> disableSummon() {
		this.summonable = false;
		return this;
	}

	/**
	 * Sets this entity type to not be saved on unload.
	 *
	 * @return this builder for chaining
	 */
	public QuiltEntityTypeBuilder<T> disableSaving() {
		this.saveable = false;
		return this;
	}

	/**
	 * Sets this entity type to be fire immune.
	 *
	 * @return this builder for chaining
	 */
	public QuiltEntityTypeBuilder<T> makeFireImmune() {
		this.fireImmune = true;
		return this;
	}

	/**
	 * Sets whether this entity type can be spawned far away from a player.
	 *
	 * @return this builder for chaining
	 */
	public QuiltEntityTypeBuilder<T> spawnableFarFromPlayer() {
		this.spawnableFarFromPlayer = true;
		return this;
	}

	/**
	 * Sets the dimensions of this entity type.
	 *
	 * @param dimensions the dimensions representing the entity's size
	 * @return this builder for chaining
	 */
	public QuiltEntityTypeBuilder<T> setDimensions(EntityDimensions dimensions) {
		Objects.requireNonNull(dimensions, "Cannot set null dimensions");
		this.dimensions = dimensions;
		return this;
	}

	/**
	 * Sets the maximum chunk tracking range of this entity type.
	 *
	 * @param range the tracking range in chunks
	 * @return this builder for chaining
	 */
	public QuiltEntityTypeBuilder<T> maxChunkTrackingRange(int range) {
		this.maxTrackingRange = range;
		return this;
	}

	/**
	 * Sets the maximum block range at which players can see this entity type.
	 * <p>
	 * This gets rounded up to the next integer radius in chunks.
	 *
	 * @param range the tracking range in blocks
	 * @return this builder for chaining
	 */
	public QuiltEntityTypeBuilder<T> maxBlockTrackingRange(int range) {
		return this.maxChunkTrackingRange((range + 15) / 16);
	}

	/**
	 * Sets the interval in ticks that entities of this entity type have their position updated to the client.
	 *
	 * @param interval the interval in ticks the entity position is updated
	 * @return this builder for chaining
	 */
	public QuiltEntityTypeBuilder<T> trackingTickInterval(int interval) {
		this.trackingTickInterval = interval;
		return this;
	}

	/**
	 * Sets whether this entity type should always update velocity to the client on a tracked tick.
	 * <p>
	 * This respects {@link QuiltEntityTypeBuilder#trackingTickInterval}.
	 *
	 * @param alwaysUpdateVelocity {@code true} if this entity type should always update velocity to the client on a tracked tick, or {@code false} otherwise
	 * @return this builder for chaining
	 */
	public QuiltEntityTypeBuilder<T> alwaysUpdateVelocity(boolean alwaysUpdateVelocity) {
		this.alwaysUpdateVelocity = alwaysUpdateVelocity;
		return this;
	}

	/**
	 * Allows this type of entity to spawn inside the given block, bypassing the default
	 * wither rose, sweet berry bush, cactus, and fire-damage-dealing blocks for
	 * non-fire-resistant mobs.
	 *
	 * <p>
	 * {@code minecraft:prevent_mob_spawning_inside} tag overrides this.
	 * With this setting, fire-resistant mobs can spawn on/in fire damage dealing blocks,
	 * and wither skeletons can spawn in wither roses. If a block added is not in the default
	 * blacklist, the addition has no effect.
	 */
	public QuiltEntityTypeBuilder<T> allowSpawningInside(Block... blocks) {
		this.canSpawnInside = ImmutableSet.copyOf(blocks);
		return this;
	}

	/**
	 * Entities of this will not spawn or load, will have their spawn eggs disabled,
	 * and will not be accessible by ID in commands, unless all feature flags
	 * in {@code requiredFlags} are present.
	 */
	public QuiltEntityTypeBuilder<T> requiredFlags(FeatureFlag... flags) {
		this.requiredFlags = FeatureFlags.MAIN_REGISTRY.bitSetOf(flags);
		return this;
	}

	/**
	 * Creates the entity type.
	 *
	 * @return a new {@link EntityType}
	 */
	public EntityType<T> build() {
		if (this.saveable) {
			// TODO: Implement once DataFixer API is available.
		}

		return new QuiltEntityType<>(this.factory, this.spawnGroup, this.saveable, this.summonable, this.fireImmune, this.spawnableFarFromPlayer, this.canSpawnInside, this.dimensions, this.maxTrackingRange, this.trackingTickInterval, this.alwaysUpdateVelocity, this.requiredFlags);
	}

	/**
	 * An extended version of {@link QuiltEntityTypeBuilder} with support for features present on {@link LivingEntity living entities}, such as default attributes.
	 *
	 * @param <T> Entity class
	 */
	public static class Living<T extends LivingEntity> extends QuiltEntityTypeBuilder<T> {
		private DefaultAttributeContainer.Builder defaultAttributeBuilder;

		protected Living(@NotNull SpawnGroup spawnGroup, @NotNull EntityType.EntityFactory<T> function) {
			super(spawnGroup, function);
		}

		/**
		 * Sets the spawn group for this entity type.
		 *
		 * @param group the spawn group
		 * @return this builder for chaining
		 */
		@Override
		public QuiltEntityTypeBuilder.Living<T> spawnGroup(@NotNull SpawnGroup group) {
			super.spawnGroup(group);
			return this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <N extends T> QuiltEntityTypeBuilder.Living<N> entityFactory(@NotNull EntityType.EntityFactory<N> factory) {
			super.entityFactory(factory);
			return (Living<N>) this;
		}

		@Override
		public QuiltEntityTypeBuilder.Living<T> disableSummon() {
			super.disableSummon();
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Living<T> disableSaving() {
			super.disableSaving();
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Living<T> makeFireImmune() {
			super.makeFireImmune();
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Living<T> spawnableFarFromPlayer() {
			super.spawnableFarFromPlayer();
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Living<T> setDimensions(EntityDimensions dimensions) {
			super.setDimensions(dimensions);
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Living<T> maxChunkTrackingRange(int range) {
			super.maxChunkTrackingRange(range);
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Living<T> maxBlockTrackingRange(int range) {
			super.maxBlockTrackingRange(range);
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Living<T> trackingTickInterval(int rate) {
			super.trackingTickInterval(rate);
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Living<T> alwaysUpdateVelocity(boolean alwaysUpdateVelocity) {
			super.alwaysUpdateVelocity(alwaysUpdateVelocity);
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Living<T> allowSpawningInside(Block... blocks) {
			super.allowSpawningInside(blocks);
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Living<T> requiredFlags(FeatureFlag... flags) {
			super.requiredFlags(flags);
			return this;
		}

		/**
		 * Sets the default attributes for a type of living entity.
		 *
		 * <p>This can be used in a fashion similar to this:</p>
		 * <blockquote><pre>
		 * QuiltEntityTypeBuilder.createLiving()
		 * 	.spawnGroup(SpawnGroup.CREATURE)
		 * 	.entityFactory(MyCreature::new)
		 * 	.defaultAttributes(LivingEntity::createLivingAttributes)
		 * 	...
		 * 	.build();
		 * </pre></blockquote>
		 *
		 * @param defaultAttributeBuilder a function to generate the default attribute builder from the entity type
		 * @return this builder for chaining
		 */
		public QuiltEntityTypeBuilder.Living<T> defaultAttributes(@NotNull DefaultAttributeContainer.Builder defaultAttributeBuilder) {
			Objects.requireNonNull(defaultAttributeBuilder, "Cannot set null attribute builder");
			this.defaultAttributeBuilder = defaultAttributeBuilder;
			return this;
		}

		@Override
		public EntityType<T> build() {
			final EntityType<T> type = super.build();

			if (this.defaultAttributeBuilder != null) {
				DefaultAttributeRegistry.DEFAULT_ATTRIBUTE_REGISTRY.put(type, this.defaultAttributeBuilder.build());
			}

			return type;
		}
	}

	/**
	 * An extended version of {@link QuiltEntityTypeBuilder} with support for features present on {@link MobEntity mob entities}, such as spawn restrictions.
	 *
	 * @param <T> Entity class
	 */
	public static class Mob<T extends MobEntity> extends QuiltEntityTypeBuilder.Living<T> {
		private SpawnRestriction.Location restrictionLocation;
		private Heightmap.Type restrictionHeightmap;
		private SpawnRestriction.SpawnPredicate<T> spawnPredicate;

		protected Mob(@NotNull SpawnGroup spawnGroup, @NotNull EntityType.EntityFactory<T> function) {
			super(spawnGroup, function);
		}

		@Override
		public QuiltEntityTypeBuilder.Mob<T> spawnGroup(@NotNull SpawnGroup group) {
			super.spawnGroup(group);
			return this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <N extends T> QuiltEntityTypeBuilder.Mob<N> entityFactory(@NotNull EntityType.EntityFactory<N> factory) {
			super.entityFactory(factory);
			return (Mob<N>) this;
		}

		@Override
		public QuiltEntityTypeBuilder.Mob<T> disableSummon() {
			super.disableSummon();
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Mob<T> disableSaving() {
			super.disableSaving();
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Mob<T> makeFireImmune() {
			super.makeFireImmune();
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Mob<T> spawnableFarFromPlayer() {
			super.spawnableFarFromPlayer();
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Mob<T> setDimensions(EntityDimensions dimensions) {
			super.setDimensions(dimensions);
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Mob<T> maxChunkTrackingRange(int range) {
			super.maxChunkTrackingRange(range);
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Mob<T> maxBlockTrackingRange(int range) {
			super.maxBlockTrackingRange(range);
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Mob<T> trackingTickInterval(int rate) {
			super.trackingTickInterval(rate);
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Mob<T> alwaysUpdateVelocity(boolean alwaysUpdateVelocity) {
			super.alwaysUpdateVelocity(alwaysUpdateVelocity);
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Mob<T> allowSpawningInside(Block... blocks) {
			super.allowSpawningInside(blocks);
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Living<T> requiredFlags(FeatureFlag... flags) {
			super.requiredFlags(flags);
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Mob<T> defaultAttributes(@NotNull DefaultAttributeContainer.Builder defaultAttributeBuilder) {
			super.defaultAttributes(defaultAttributeBuilder);
			return this;
		}

		/**
		 * Registers a spawn restriction for this entity.
		 * <p>
		 * This is used by mobs to determine whether Minecraft should spawn an entity within a certain context.
		 *
		 * @param location the type of location for this entity type to spawn in
		 * @param heightmap what part of the heightmap for this entity type to spawn in
		 * @param spawnPredicate the conditions for a successful entity spawn
		 * @return this builder for chaining
		 */
		public QuiltEntityTypeBuilder.Mob<T> spawnRestriction(@NotNull SpawnRestriction.Location location, @NotNull Heightmap.Type heightmap, @NotNull SpawnRestriction.SpawnPredicate<T> spawnPredicate) {
			this.restrictionLocation = Objects.requireNonNull(location, "Location cannot be null.");
			this.restrictionHeightmap = Objects.requireNonNull(heightmap, "Heightmap type cannot be null.");
			this.spawnPredicate = Objects.requireNonNull(spawnPredicate, "Spawn predicate cannot be null.");
			return this;
		}

		@Override
		public EntityType<T> build() {
			EntityType<T> type = super.build();

			if (this.spawnPredicate != null) {
				SpawnRestriction.register(type, this.restrictionLocation, this.restrictionHeightmap, this.spawnPredicate);
			}

			return type;
		}
	}
}
