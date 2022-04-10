package org.quiltmc.qsl.registry.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.util.collection.IdList;
import org.quiltmc.qsl.registry.impl.sync.SynchronizedIdList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(IdList.class)
public class IdListMixin<T> implements SynchronizedIdList {

	@Shadow
	private int nextId;

	@Shadow
	@Final
	private List<T> list;

	@Shadow
	@Final
	private Object2IntMap<T> idMap;

	@Override
	public void quilt$clear() {
		this.nextId = 0;
		this.list.clear();
		this.idMap.clear();
	}
}
