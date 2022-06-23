package org.quiltmc.qsl.component.api;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.level.LevelProperties;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.base.api.util.InjectedInterface;
import org.quiltmc.qsl.component.api.identifier.ComponentIdentifier;

import java.util.Optional;

@InjectedInterface({
		BlockEntity.class,
		Entity.class,
		Chunk.class,
		LevelProperties.class
})
public interface ComponentProvider {
	@NotNull
	ComponentContainer getContainer();

	default <C extends Component> Optional<C> expose(ComponentIdentifier<C> id) {
		return Components.expose(id, this);
	}
}
