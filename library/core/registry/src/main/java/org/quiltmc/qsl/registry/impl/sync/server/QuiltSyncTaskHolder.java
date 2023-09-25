package org.quiltmc.qsl.registry.impl.sync.server;

public interface QuiltSyncTaskHolder {
	QuiltSyncTask qsl$getSyncTask();
	void qsl$finishSyncTask();
}
