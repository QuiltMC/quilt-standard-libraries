package org.quiltmc.qsl.registry.impl.sync;

import net.minecraft.util.collection.IdList;

public interface SynchronizedIdList<T> {
	void quilt$clear();

	static <T> SynchronizedIdList<T> as(IdList<T> idList) {
		return (SynchronizedIdList<T>) idList;
	}

	static void clear(IdList<?> idList) {
		((SynchronizedIdList<?>) idList).quilt$clear();
	}
}
