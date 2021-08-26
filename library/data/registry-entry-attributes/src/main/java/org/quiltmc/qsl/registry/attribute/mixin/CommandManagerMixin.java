package org.quiltmc.qsl.registry.attribute.mixin;

import com.mojang.brigadier.CommandDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.quiltmc.qsl.registry.attribute.impl.DumpBuiltinAttributesCommand;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

// FIXME replace with command registration event once that's implemented
@Mixin(CommandManager.class)
public abstract class CommandManagerMixin {
	@Shadow
	@Final
	private CommandDispatcher<ServerCommandSource> dispatcher;

	@Inject(method = "<init>", at = @At("RETURN"))
	public void qsl$registerDumpCommand(CommandManager.RegistrationEnvironment environment, CallbackInfo ci) {
		DumpBuiltinAttributesCommand.register(dispatcher);
	}
}
