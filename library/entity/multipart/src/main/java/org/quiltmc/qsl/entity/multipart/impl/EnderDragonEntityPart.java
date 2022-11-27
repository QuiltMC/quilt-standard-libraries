package org.quiltmc.qsl.entity.multipart.impl;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;

import org.quiltmc.qsl.base.api.util.InjectedInterface;
import org.quiltmc.qsl.entity.multipart.api.EntityPart;

@InjectedInterface(EnderDragonPart.class)
public interface EnderDragonEntityPart extends EntityPart<EnderDragonEntity> {
	@Override
	default EnderDragonEntity getOwner() {
		throw new UnsupportedOperationException("No implementation of getOwner could be found.");
	}
}
