package org.quiltmc.qsl.networking.impl.codec;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

import net.minecraft.network.PacketByteBuf;

import org.quiltmc.qsl.networking.api.codec.NetworkCodec;

public class MapNetworkCodec<K, V> implements NetworkCodec<Map<K, V>> {
	private final EntryCodec<K, V> entryCodec;
	private final IntFunction<? extends Map<K, V>> mapFactory;

	public MapNetworkCodec(EntryCodec<K, V> entryCodec, IntFunction<? extends Map<K, V>> mapFactory) {
		this.entryCodec = entryCodec;
		this.mapFactory = mapFactory;
	}

	@Override
	public Map<K, V> decode(PacketByteBuf buf) {
		int size = buf.readVarInt();
		Map<K, V> map = this.mapFactory.apply(size);

		for (int i = 0; i < size; i++) {
			map.put(this.entryCodec.decodeKey(buf), this.entryCodec.decodeValue(buf));
		}

		return map;
	}

	@Override
	public void encode(PacketByteBuf buf, Map<K, V> data) {
		buf.writeVarInt(data.size());

		for (Map.Entry<K, V> entry : data.entrySet()) {
			this.entryCodec.encode(buf, entry);
		}
	}

	@Override
	public String toString() {
		return "MapNetworkCodec[" + this.entryCodec + "]";
	}

	public static class EntryCodec<K, V> implements NetworkCodec<Map.Entry<K, V>> {
		private final NetworkCodec<K> keyCodec;
		private final NetworkCodec<V> valueCodec;

		public EntryCodec(NetworkCodec<K> keyCodec, NetworkCodec<V> valueCodec) {
			this.keyCodec = keyCodec;
			this.valueCodec = valueCodec;
		}

		@Override
		public Map.Entry<K, V> decode(PacketByteBuf buf) {
			K key = this.keyCodec.decode(buf);
			V value = this.valueCodec.decode(buf);

			return new Map.Entry<>() {
				@Override
				public K getKey() {
					return key;
				}

				@Override
				public V getValue() {
					return value;
				}

				@Override
				public V setValue(V value) {
					throw new UnsupportedOperationException();
				}
			};
		}

		public K decodeKey(PacketByteBuf buf) {
			return this.keyCodec.decode(buf);
		}

		public V decodeValue(PacketByteBuf buf) {
			return this.valueCodec.decode(buf);
		}

		@Override
		public void encode(PacketByteBuf buf, Map.Entry<K, V> data) {
			this.keyCodec.encode(buf, data.getKey());
			this.valueCodec.encode(buf, data.getValue());
		}

		public void encodeKeyValue(PacketByteBuf buf, K key, V value) {
			this.keyCodec.encode(buf, key);
			this.valueCodec.encode(buf, value);
		}

		public MapNetworkCodec<K, V> intoMap() {
			return this.intoMap(HashMap::new);
		}

		public MapNetworkCodec<K, V> intoMap(IntFunction<? extends Map<K ,V>> mapFactory) {
			return new MapNetworkCodec<>(this, mapFactory);
		}

		public PairNetworkCodec<K ,V> intoPair() {
			return this.keyCodec.pairWith(this.valueCodec);
		}

		@Override
		public String toString() {
			return "EntryCodec{key=%s, value=%S}".formatted(this.keyCodec, this.valueCodec);
		}
	}
}
