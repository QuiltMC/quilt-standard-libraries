package org.quiltmc.qsl.component.mixin.world;

import net.minecraft.world.World;
import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.impl.util.ComponentProviderState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(World.class)
public class WorldMixin implements ComponentProvider {
	@Override
	public ComponentContainer getComponentContainer() {
		return ComponentProviderState.get(this).getComponentContainer();
	}
}
