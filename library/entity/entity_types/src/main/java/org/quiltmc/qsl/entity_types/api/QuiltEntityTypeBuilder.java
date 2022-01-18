/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.entity_types.api;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.quiltmc.qsl.entity_attributes.api.QuiltDefaultAttributeRegistry;
import org.quiltmc.qsl.entity_types.impl.QuiltEntityType;
import org.quiltmc.qsl.entity_types.mixin.SpawnRestrictionAccessor;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Extended version of {@link EntityType.Builder} with added registration for
 * server-&gt;client entity tracking values.
 *
 * @param <T> Entity class.
 */
public class QuiltEntityTypeBuilder<T extends Entity> {
	private SpawnGroup spawnGroup;
	private EntityType.EntityFactory<T> factory;
	private boolean saveable = true;
	private boolean summonable = true;
	private int trackRange = 5;
	private int trackedUpdateRate = 3;
	private Boolean forceTrackedVelocityUpdates;
	private boolean fireImmune = false;
	private boolean spawnableFarFromPlayer;
	private EntityDimensions dimensions = EntityDimensions.changing(-1.0f, -1.0f);
	private ImmutableSet<Block> specificSpawnBlocks = ImmutableSet.of();

	protected QuiltEntityTypeBuilder(SpawnGroup spawnGroup, EntityType.EntityFactory<T> factory) {
		this.spawnGroup = spawnGroup;
		this.factory = factory;
		this.spawnableFarFromPlayer = spawnGroup == SpawnGroup.CREATURE || spawnGroup == SpawnGroup.MISC;
	}

	/**
	 * Creates an entity type builder.
	 *
	 * <p>This entity's spawn group will automatically be set to {@link SpawnGroup#MISC}.
	 *
	 * @param <T> the type of entity
	 *
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
	 *
	 * @return a new entity type builder
	 */
	public static <T extends Entity> QuiltEntityTypeBuilder<T> create(SpawnGroup spawnGroup) {
		return create(spawnGroup, QuiltEntityTypeBuilder::emptyFactory);
	}

	/**
	 * Creates an entity type builder.
	 *
	 * @param spawnGroup the entity spawn group
	 * @param factory the entity factory used to create this entity
	 * @param <T> the type of entity
	 *
	 * @return a new entity type builder
	 */
	public static <T extends Entity> QuiltEntityTypeBuilder<T> create(SpawnGroup spawnGroup, EntityType.EntityFactory<T> factory) {
		return new QuiltEntityTypeBuilder<>(spawnGroup, factory);
	}

	/**
	 * Creates an entity type builder for a living entity.
	 *
	 * <p>This entity's spawn group will automatically be set to {@link SpawnGroup#MISC}.
	 *
	 * @param <T> the type of entity
	 *
	 * @return a new living entity type builder
	 */
	public static <T extends LivingEntity> QuiltEntityTypeBuilder.Living<T> createLiving() {
		return new QuiltEntityTypeBuilder.Living<>(SpawnGroup.MISC, QuiltEntityTypeBuilder::emptyFactory);
	}

	/**
	 * Creates an entity type builder for a mob entity.
	 *
	 * @param <T> the type of entity
	 *
	 * @return a new mob entity type builder
	 */
	public static <T extends MobEntity> QuiltEntityTypeBuilder.Mob<T> createMob() {
		return new QuiltEntityTypeBuilder.Mob<>(SpawnGroup.MISC, QuiltEntityTypeBuilder::emptyFactory);
	}

	private static <T extends Entity> T emptyFactory(EntityType<T> type, World world) {
		return null;
	}

	public QuiltEntityTypeBuilder<T> spawnGroup(SpawnGroup group) {
		Objects.requireNonNull(group, "Spawn group cannot be null");
		this.spawnGroup = group;
		return this;
	}

	public <N extends T> QuiltEntityTypeBuilder<N> entityFactory(EntityType.EntityFactory<N> factory) {
		Objects.requireNonNull(factory, "Entity Factory cannot be null");
		this.factory = (EntityType.EntityFactory<T>) factory;
		return (QuiltEntityTypeBuilder<N>) this;
	}

	/**
	 * Whether this entity type is summonable using the {@code /summon} command.
	 *
	 * @return this builder for chaining
	 */
	public QuiltEntityTypeBuilder<T> disableSummon() {
		this.summonable = false;
		return this;
	}

	public QuiltEntityTypeBuilder<T> disableSaving() {
		this.saveable = false;
		return this;
	}

	/**
	 * Sets this entity type to be fire immune.
	 *
	 * @return this builder for chaining
	 */
	public QuiltEntityTypeBuilder<T> fireImmune() {
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
	 *
	 * @return this builder for chaining
	 */
	public QuiltEntityTypeBuilder<T> dimensions(EntityDimensions dimensions) {
		Objects.requireNonNull(dimensions, "Cannot set null dimensions");
		this.dimensions = dimensions;
		return this;
	}

	/**
	 * Sets the maximum chunk tracking range of this entity type.
	 *
	 * @param range the tracking range in chunks
	 *
	 * @return this builder for chaining
	 */
	public QuiltEntityTypeBuilder<T> trackRangeChunks(int range) {
		this.trackRange = range;
		return this;
	}

	/**
	 * Sets the maximum block range at which players can see this entity type.
	 *
	 * @param range the tracking range in blocks
	 *
	 * @return this builder for chaining
	 */
	public QuiltEntityTypeBuilder<T> trackRangeBlocks(int range) {
		return trackRangeChunks((range + 15) / 16);
	}

	public QuiltEntityTypeBuilder<T> trackedUpdateRate(int rate) {
		this.trackedUpdateRate = rate;
		return this;
	}

	public QuiltEntityTypeBuilder<T> forceTrackedVelocityUpdates(boolean forceTrackedVelocityUpdates) {
		this.forceTrackedVelocityUpdates = forceTrackedVelocityUpdates;
		return this;
	}

	/**
	 * Sets the {@link ImmutableSet} of blocks this entity can spawn on.
	 *
	 * @param blocks the blocks the entity can spawn on
	 * @return this builder for chaining
	 */
	public QuiltEntityTypeBuilder<T> specificSpawnBlocks(Block... blocks) {
		this.specificSpawnBlocks = ImmutableSet.copyOf(blocks);
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

		return new QuiltEntityType<>(this.factory, this.spawnGroup, this.saveable, this.summonable, this.fireImmune, this.spawnableFarFromPlayer, this.specificSpawnBlocks, dimensions, trackRange, trackedUpdateRate, forceTrackedVelocityUpdates);
	}

	/**
	 * An extended version of {@link QuiltEntityTypeBuilder} with support for features on present on {@link LivingEntity living entities}, such as default attributes.
	 *
	 * @param <T> Entity class.
	 */
	public static class Living<T extends LivingEntity> extends QuiltEntityTypeBuilder<T> {
		private Supplier<DefaultAttributeContainer.Builder> defaultAttributeBuilder;

		protected Living(SpawnGroup spawnGroup, EntityType.EntityFactory<T> function) {
			super(spawnGroup, function);
		}

		@Override
		public QuiltEntityTypeBuilder.Living<T> spawnGroup(SpawnGroup group) {
			super.spawnGroup(group);
			return this;
		}

		@Override
		public <N extends T> QuiltEntityTypeBuilder.Living<N> entityFactory(EntityType.EntityFactory<N> factory) {
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
		public QuiltEntityTypeBuilder.Living<T> fireImmune() {
			super.fireImmune();
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Living<T> spawnableFarFromPlayer() {
			super.spawnableFarFromPlayer();
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Living<T> dimensions(EntityDimensions dimensions) {
			super.dimensions(dimensions);
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Living<T> trackRangeChunks(int range) {
			super.trackRangeChunks(range);
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Living<T> trackRangeBlocks(int range) {
			super.trackRangeBlocks(range);
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Living<T> trackedUpdateRate(int rate) {
			super.trackedUpdateRate(rate);
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Living<T> forceTrackedVelocityUpdates(boolean forceTrackedVelocityUpdates) {
			super.forceTrackedVelocityUpdates(forceTrackedVelocityUpdates);
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Living<T> specificSpawnBlocks(Block... blocks) {
			super.specificSpawnBlocks(blocks);
			return this;
		}

		/**
		 * Sets the default attributes for a type of living entity.
		 *
		 * <p>This can be used in a fashion similar to this:
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
		public QuiltEntityTypeBuilder.Living<T> defaultAttributes(Supplier<DefaultAttributeContainer.Builder> defaultAttributeBuilder) {
			Objects.requireNonNull(defaultAttributeBuilder, "Cannot set null attribute builder");
			this.defaultAttributeBuilder = defaultAttributeBuilder;
			return this;
		}

		@Override
		public EntityType<T> build() {
			final EntityType<T> type = super.build();

			if (this.defaultAttributeBuilder != null) {
				QuiltDefaultAttributeRegistry.register(type, this.defaultAttributeBuilder.get());
			}

			return type;
		}
	}

	/**
	 * An extended version of {@link QuiltEntityTypeBuilder} with support for features on present on {@link MobEntity mob entities}, such as spawn restrictions.
	 *
	 * @param <T> Entity class.
	 */
	public static class Mob<T extends MobEntity> extends QuiltEntityTypeBuilder.Living<T> {
		private SpawnRestriction.Location restrictionLocation;
		private Heightmap.Type restrictionHeightmap;
		private SpawnRestriction.SpawnPredicate<T> spawnPredicate;

		protected Mob(SpawnGroup spawnGroup, EntityType.EntityFactory<T> function) {
			super(spawnGroup, function);
		}

		@Override
		public QuiltEntityTypeBuilder.Mob<T> spawnGroup(SpawnGroup group) {
			super.spawnGroup(group);
			return this;
		}

		@Override
		public <N extends T> QuiltEntityTypeBuilder.Mob<N> entityFactory(EntityType.EntityFactory<N> factory) {
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
		public QuiltEntityTypeBuilder.Mob<T> fireImmune() {
			super.fireImmune();
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Mob<T> spawnableFarFromPlayer() {
			super.spawnableFarFromPlayer();
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Mob<T> dimensions(EntityDimensions dimensions) {
			super.dimensions(dimensions);
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Mob<T> trackRangeChunks(int range) {
			super.trackRangeChunks(range);
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Mob<T> trackRangeBlocks(int range) {
			super.trackRangeBlocks(range);
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Mob<T> trackedUpdateRate(int rate) {
			super.trackedUpdateRate(rate);
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Mob<T> forceTrackedVelocityUpdates(boolean forceTrackedVelocityUpdates) {
			super.forceTrackedVelocityUpdates(forceTrackedVelocityUpdates);
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Mob<T> specificSpawnBlocks(Block... blocks) {
			super.specificSpawnBlocks(blocks);
			return this;
		}

		@Override
		public QuiltEntityTypeBuilder.Mob<T> defaultAttributes(Supplier<DefaultAttributeContainer.Builder> defaultAttributeBuilder) {
			super.defaultAttributes(defaultAttributeBuilder);
			return this;
		}

		/**
		 * Registers a spawn restriction for this entity.
		 *
		 * <p>This is used by mobs to determine whether Minecraft should spawn an entity within a certain context.
		 *
		 * @return this builder for chaining.
		 */
		public QuiltEntityTypeBuilder.Mob<T> spawnRestriction(SpawnRestriction.Location location, Heightmap.Type heightmap, SpawnRestriction.SpawnPredicate<T> spawnPredicate) {
			this.restrictionLocation = Objects.requireNonNull(location, "Location cannot be null.");
			this.restrictionHeightmap = Objects.requireNonNull(heightmap, "Heightmap type cannot be null.");
			this.spawnPredicate = Objects.requireNonNull(spawnPredicate, "Spawn predicate cannot be null.");
			return this;
		}

		@Override
		public EntityType<T> build() {
			EntityType<T> type = super.build();

			if (this.spawnPredicate != null) {
				SpawnRestrictionAccessor.callRegister(type, this.restrictionLocation, this.restrictionHeightmap, this.spawnPredicate);
			}

			return type;
		}
	}
}
