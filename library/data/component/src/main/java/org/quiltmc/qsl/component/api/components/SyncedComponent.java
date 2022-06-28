package org.quiltmc.qsl.component.api.components;

import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.Component;

public interface SyncedComponent extends Component {
	void writeToBuf(@NotNull PacketByteBuf buf);

	void readFromBuf(@NotNull PacketByteBuf buf);

	@Nullable Runnable getSyncOperation();

	void setSyncOperation(@NotNull Runnable runnable);

	default void sync() {
		if (this.getSyncOperation() != null) {
			this.getSyncOperation().run();
		}
	}
}
