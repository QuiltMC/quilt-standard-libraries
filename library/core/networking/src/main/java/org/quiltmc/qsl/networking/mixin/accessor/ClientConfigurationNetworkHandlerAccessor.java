package org.quiltmc.qsl.networking.mixin.accessor;

import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.network.ClientConfigurationNetworkHandler;

@Mixin(ClientConfigurationNetworkHandler.class)
public interface ClientConfigurationNetworkHandlerAccessor extends AbstractClientNetworkHandlerAccessor {
	@Accessor("localGameProfile")
	GameProfile getProfile();
}
