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

package org.quiltmc.qsl.signal_channel.impl;

import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.quiltmc.qsl.signal_channel.api.SignalChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class WorldSubscriptionManager {
	//TODO move tracked ones out of here
	private Map<SignalChannel<?>, List<SubscriptionImpl.Unnamed<?>>> unnamedSubscriptions;
	private Map<Identifier, List<SubscriptionImpl.Named<?>>> namedSubscriptions;

	public static Map<World, WorldSubscriptionManager> managers = new HashMap<>();

	private World world;

	public WorldSubscriptionManager(World world) {
		this.world = world;
		unnamedSubscriptions = new HashMap<>();
		namedSubscriptions = new HashMap<>();
	}

	private void cleanSubscriptionMaps() {
		unnamedSubscriptions = cleanMap(unnamedSubscriptions);
		namedSubscriptions = cleanMap(namedSubscriptions);
	}

	private <K, V extends SubscriptionImpl<?>> Map<K, List<V>> cleanMap(Map<K, List<V>> map) {
		return map.entrySet()
				.stream()
				.map(e -> new Pair<>(
								e.getKey(),
								e.getValue().stream()
										.filter(s -> !s.shouldRemove())
										.collect(Collectors.toCollection(ArrayList::new))
						)
				)
				.collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
	}

	private <T extends SubscriptionImpl<?>> List<T> filterByLocation(List<T> subscriptions, Predicate<Vec3d> positionFilter) {
		return subscriptions.stream()
				.filter(s ->
						s.getTracker() == null ||
								positionFilter.test(s.getTracker().getPosition())
				).toList();
	}

	public List<SubscriptionImpl.Named<?>> getNamedSubscriptions(Identifier id) {
		cleanSubscriptionMaps();
		return namedSubscriptions.computeIfAbsent(id, k -> new ArrayList<>()); //TODO change raw gets to computes!
	}

	public List<SubscriptionImpl.Named<?>> getNamedSubscriptionsInRange(Identifier id, Vec3d origin, double range) {
		cleanSubscriptionMaps();
		return filterByLocation(getNamedSubscriptions(id), pos -> pos.distanceTo(origin) <= range);
	}

	public List<SubscriptionImpl.Named<?>> getNamedSubscriptionsInBox(Identifier id, Box box) {
		cleanSubscriptionMaps();
		return filterByLocation(getNamedSubscriptions(id), box::contains);
	}

	@SuppressWarnings("unchecked")
	public <T> List<SubscriptionImpl.Unnamed<T>> getUnnamedSubscriptions(SignalChannel<T> channel) {
		cleanSubscriptionMaps();
		return unnamedSubscriptions.computeIfAbsent(channel, k -> new ArrayList<>()).stream()
				.map(s -> (SubscriptionImpl.Unnamed<T>) s)
				.toList();
	}

	public <T> List<SubscriptionImpl.Unnamed<T>> getUnnamedSubscriptionsInRange(SignalChannel<T> channel, Vec3d origin, double range) {
		cleanSubscriptionMaps();
		return filterByLocation(getUnnamedSubscriptions(channel), pos -> pos.distanceTo(origin) <= range);
	}

	public <T> List<SubscriptionImpl.Unnamed<T>> getUnnamedSubscriptionsInBox(SignalChannel<T> channel, Box box) {
		cleanSubscriptionMaps();
		return filterByLocation(getUnnamedSubscriptions(channel), box::contains);
	}

	public void addNamedSubscription(Identifier id, SubscriptionImpl.Named<?> subscription) {
		cleanSubscriptionMaps();
		namedSubscriptions.computeIfAbsent(id, k -> new ArrayList<>()).add(subscription);
	}

	public <T> void addUnnamedSubscription(SignalChannel<T> channel, SubscriptionImpl.Unnamed<T> subscription) {
		cleanSubscriptionMaps();
		unnamedSubscriptions.computeIfAbsent(channel, k -> new ArrayList<>()).add(subscription);
	}
}
