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

package org.quiltmc.qsl.data.callback.test;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.data.callback.api.CodecAware;
import org.quiltmc.qsl.data.callback.api.CodecMap;
import org.quiltmc.qsl.data.callback.api.DynamicEventCallbackSource;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.reloader.SimpleSynchronousResourceReloader;

public class DataCallbackTests implements ModInitializer {
	public static final Identifier BEFORE_PHASE = new Identifier("quilt_data_callback_testmod", "before_phase");
	public static final Identifier AFTER_PHASE = new Identifier("quilt_data_callback_testmod", "after_phase");

	// We make our own event here because we want our callback to have the CodecAware interface. In an actual usage, this
	// would be added to the base event itself instead; however, this is a test mod, and we would have to make a whole
	// new useful server-side event otherwise.
	public static final Event<ServerJoin> SERVER_JOIN = Event.createWithPhases(ServerJoin.class, callbacks -> (handler, sender, server) -> {
		for (ServerJoin callback : callbacks) {
			callback.onPlayReady(handler, sender, server);
		}
	}, BEFORE_PHASE, Event.DEFAULT_PHASE, AFTER_PHASE);

	public static final CodecMap<ServerJoin> JOIN_SERVER_CODECS = new CodecMap<>((handler, sender, server) -> {});
	public static DynamicEventCallbackSource<ServerJoin> JOIN_SERVER_DATA = new DynamicEventCallbackSource<>(new Identifier("quilt_data_callback_testmod", "server_join"), JOIN_SERVER_CODECS, ServerJoin.class, SERVER_JOIN, callbacks -> (handler, sender, server) -> {
		for (ServerJoin callback : callbacks.get()) {
			callback.onPlayReady(handler, sender, server);
		}
	});

	@Override
	public void onInitialize(ModContainer mod) {
		JOIN_SERVER_CODECS.register(ServerJoinChat.CODEC_ID, ServerJoinChat.CODEC);

		ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) ->
				SERVER_JOIN.invoker().onPlayReady(handler, sender, server)));

		JOIN_SERVER_DATA.register(new Identifier(mod.metadata().id(), "after"), new ServerJoinChat("Registered in the after phase from code!", Style.EMPTY), AFTER_PHASE);
		// This callback is overridden by data and should not fire.
		JOIN_SERVER_DATA.register(new Identifier(mod.metadata().id(), "overridden"), (handler, sender, server) -> {
			throw new RuntimeException("This callback should have been overridden by data!");
		});

		ResourceLoader.get(ResourceType.SERVER_DATA).registerReloader(new SimpleSynchronousResourceReloader() {
			@Override
			public void reload(ResourceManager manager) {
				JOIN_SERVER_DATA.update(manager);
			}

			@Override
			public @NotNull Identifier getQuiltId() {
				return new Identifier("quilt_data_callback_testmod", "server_join_callbacks_listener");
			}
		});
	}

	// This is a duplicate of ServerPlayConnectionEvents.Join that implements CodecAware
	public interface ServerJoin extends CodecAware {
		void onPlayReady(ServerPlayNetworkHandler handler, PacketSender<CustomPayload> sender, MinecraftServer server);
	}

	public record ServerJoinChat(String text, Style style) implements ServerJoin {
		public static final Codec<ServerJoinChat> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.STRING.fieldOf("text").forGetter(ServerJoinChat::text), Style.Serializer.CODEC.fieldOf("style").forGetter(ServerJoinChat::style)).apply(instance, ServerJoinChat::new));
		public static final Identifier CODEC_ID = new Identifier("quilt_data_callback_testmod", "chat");

		@Override
		public Identifier getCodecId() {
			return CODEC_ID;
		}

		@Override
		public void onPlayReady(ServerPlayNetworkHandler handler, PacketSender<CustomPayload> sender, MinecraftServer server) {
			Text text = Text.literal(text()).setStyle(style());
			handler.player.sendSystemMessage(text);
		}
	}
}
