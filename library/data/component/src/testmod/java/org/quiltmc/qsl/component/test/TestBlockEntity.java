package org.quiltmc.qsl.component.test;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.ComponentContainer;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.Components;
import org.quiltmc.qsl.component.api.components.IntegerComponent;
import org.quiltmc.qsl.component.api.identifier.ComponentIdentifier;
import org.quiltmc.qsl.component.impl.SimpleComponentContainer;

public class TestBlockEntity extends BlockEntity implements ComponentProvider {
	public static final ComponentIdentifier<IntegerComponent> TEST_BE_INT =
			IntegerComponent.create(new Identifier("quilt_component_test", "test_be_int"));

	private final ComponentContainer container =
			SimpleComponentContainer.create(this::markDirty, TEST_BE_INT);

	public TestBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(ComponentTestMod.TEST_BE_TYPE, blockPos, blockState);
	}

	@Override
	public @NotNull ComponentContainer getContainer() {
		return this.container;
	}

	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		this.container.writeNbt(nbt);
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		this.container.readNbt(nbt);
	}

	public static <T extends BlockEntity> void tick(World world, BlockPos ignoredPos, BlockState ignoredState, T blockEntity) {
		if (world.isClient) {
			return;
		}

		Components.expose(TEST_BE_INT, blockEntity).ifPresent(IntegerComponent::increment);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		return this.toNbt();
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.of(this);
	}
}
