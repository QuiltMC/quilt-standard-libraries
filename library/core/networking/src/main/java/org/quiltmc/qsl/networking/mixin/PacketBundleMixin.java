package org.quiltmc.qsl.networking.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketBundle;

/**
 * This mixin allows for nesting PacketBundles inside each other. Normally, that
 * would throw a messy error deep in packet code. This is particularly important
 * for the Entity Networking module, and could be generally beneficial for modders.
 * <p>
 * The flattening is only needed on the server side. Packets are not re-flattened on the client.
 */
@Mixin(PacketBundle.class)
public class PacketBundleMixin {
	@ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
	private static Iterable<Packet<?>> quilt$flattenPackets(Iterable<Packet<?>> packets) {
		List<Packet<?>> list = new ArrayList<>();
		quilt$recursivelyCollectBundledPackets(packets, list);
		return list;
	}

	@Unique
	private static void quilt$recursivelyCollectBundledPackets(Iterable<Packet<?>> packets, List<Packet<?>> list) {
		for (Packet<?> packet : packets) {
			if (packet instanceof PacketBundle<?> bundle) {
				//noinspection unchecked,rawtypes
				quilt$recursivelyCollectBundledPackets((Iterable) bundle.getPackets(), list);
			} else {
				list.add(packet);
			}
		}
	}
}
