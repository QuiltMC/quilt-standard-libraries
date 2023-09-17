package org.quiltmc.qsl.networking.impl;

public interface CommonPacketHandler {
	void onVersionPacket(int negotiatedVersion);

	void onRegisterPacket(RegisterPayload payload);

	RegisterPayload createRegisterPayload();

	int getNegotiatedVersion();
}
