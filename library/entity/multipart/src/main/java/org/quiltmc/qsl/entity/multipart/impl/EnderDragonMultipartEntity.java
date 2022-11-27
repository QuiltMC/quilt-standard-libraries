package org.quiltmc.qsl.entity.multipart.impl;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;

import org.quiltmc.qsl.base.api.util.InjectedInterface;
import org.quiltmc.qsl.entity.multipart.api.EntityPart;
import org.quiltmc.qsl.entity.multipart.api.MultipartEntity;

@InjectedInterface(EnderDragonEntity.class)
public interface EnderDragonMultipartEntity extends MultipartEntity {
	@Override
	default EntityPart<?>[] getEntityParts() {
		throw new UnsupportedOperationException("No implementation of getEntityParts could be found.");
	}
}
