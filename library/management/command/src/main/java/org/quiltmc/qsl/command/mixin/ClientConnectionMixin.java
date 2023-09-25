package org.quiltmc.qsl.command.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.network.ClientConnection;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.command.impl.KnownArgTypesStorage;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin implements KnownArgTypesStorage {
	@Unique
	private Set<Identifier> quilt$knownArgumentTypes;

	@Override
	public Set<Identifier> quilt$getKnownArgumentTypes() {
		return this.quilt$knownArgumentTypes;
	}

	@Override
	public void quilt$setKnownArgumentTypes(Set<Identifier> types) {
		this.quilt$knownArgumentTypes = types;
	}
}
