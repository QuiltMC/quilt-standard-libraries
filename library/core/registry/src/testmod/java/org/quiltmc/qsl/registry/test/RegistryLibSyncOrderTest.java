/*
 * Copyright 2024 The Quilt Project
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

package org.quiltmc.qsl.registry.test;

import java.util.function.Consumer;

import net.fabricmc.api.EnvType;

import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.ServerConfigurationPacketHandler;
import net.minecraft.network.configuration.ConfigurationTask;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.base.api.entrypoint.server.DedicatedServerModInitializer;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.networking.api.CustomPayloads;
import org.quiltmc.qsl.networking.api.ServerConfigurationConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerConfigurationTaskManager;
import org.quiltmc.qsl.networking.api.client.ClientConfigurationNetworking;

/**
 * Makes sure that the registry sync is done soon enough in the configuration system.
 */
public class RegistryLibSyncOrderTest implements ModInitializer, DedicatedServerModInitializer, ClientModInitializer {
	private static final Identifier PACKET_ID = new Identifier("quilt", "reg_sync_order_packet");
	public static Item ITEM_A = new Item(new Item.Settings());
	public static Item ITEM_B = new Item(new Item.Settings());
	private static final Identifier EARLY_PHASE = new Identifier("quilt", "reg_sync_order_early");

	record TestPayload(boolean early, int a, int b) implements CustomPayload {
		TestPayload(PacketByteBuf buf) {
			this(buf.readBoolean(), buf.readInt(), buf.readInt());
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeBoolean(this.early);
			buf.writeInt(this.a);
			buf.writeInt(this.b);
		}

		@Override
		public Identifier id() {
			return PACKET_ID;
		}
	}

	@Override
	public void onInitialize(ModContainer mod) {
		if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT) {
			Registry.register(Registries.ITEM, new Identifier("quilt", "reg_sync_order_a"), ITEM_A);
			Registry.register(Registries.ITEM, new Identifier("quilt", "reg_sync_order_b"), ITEM_B);
		} else {
			Registry.register(Registries.ITEM, new Identifier("quilt", "reg_sync_order_b"), ITEM_B);
			Registry.register(Registries.ITEM, new Identifier("quilt", "reg_sync_order_a"), ITEM_A);
		}

		CustomPayloads.registerS2CPayload(PACKET_ID, TestPayload::new);
	}

	@Override
	public void onInitializeClient(ModContainer mod) {
		ClientConfigurationNetworking.registerGlobalReceiver(PACKET_ID, (ClientConfigurationNetworking.CustomChannelReceiver<TestPayload>) (client, handler, payload, responseSender) -> {
			int aID = Registries.ITEM.getRawId(ITEM_A);
			int bID = Registries.ITEM.getRawId(ITEM_B);
			if (payload.early()) {
				if (aID == payload.a()) {
					throw new RuntimeException("Item A ID matches!");
				}

				if (bID == payload.b()) {
					throw new RuntimeException("Item B ID matches!");
				}
			} else {
				if (aID != payload.a()) {
					throw new RuntimeException(String.format("Item A IDs (%d, %d) don't match!", aID, payload.a()));
				}

				if (bID != payload.b()) {
					throw new RuntimeException(String.format("Item B IDs (%d, %d) don't match!", bID, payload.b()));
				}
			}
		});
	}

	@Override
	public void onInitializeServer(ModContainer mod) {
		ServerConfigurationConnectionEvents.INIT.addPhaseOrdering(EARLY_PHASE, Event.DEFAULT_PHASE);
		ServerConfigurationConnectionEvents.INIT.register(EARLY_PHASE, (handler, server) -> {
			((ServerConfigurationTaskManager) handler).addPriorityTask(new SyncIDTask(handler, true));
		});
		ServerConfigurationConnectionEvents.READY.register((handler, sender, server) -> {
			((ServerConfigurationTaskManager) handler).addTask(new SyncIDTask(handler, false));
		});
	}

	public class SyncIDTask implements ConfigurationTask {
		private static final Type TYPE = new Type("quilt:sync_id_task");
		private final ServerConfigurationPacketHandler handler;
		private boolean early;

		public SyncIDTask(ServerConfigurationPacketHandler handler, boolean early) {
			this.early = early;
			this.handler = handler;
		}

		@Override
		public void start(Consumer<Packet<?>> task) {
			task.accept(new CustomPayloadS2CPacket(new TestPayload(this.early, Registries.ITEM.getRawId(ITEM_A), Registries.ITEM.getRawId(ITEM_B))));
			((ServerConfigurationTaskManager) this.handler).finishTask(TYPE);
		}

		@Override
		public Type getType() {
			return TYPE;
		}
	}
}
