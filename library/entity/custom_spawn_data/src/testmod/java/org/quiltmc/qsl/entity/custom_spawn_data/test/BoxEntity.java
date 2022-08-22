package org.quiltmc.qsl.entity.custom_spawn_data.test;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.quiltmc.qsl.entity.custom_spawn_data.api.AbstractCustomSpawnDataEntity;

public class BoxEntity extends AbstractCustomSpawnDataEntity {
	public BoxEntity(EntityType<?> entityType, World world) {
		super(entityType, world);
	}

	public BoxEntity(ItemStack stored, World world) {
		this(CustomSpawnDataTestInitializer.BOX_TYPE, world);
		this.stored = stored;
	}

	@Override
	protected void initDataTracker() {
	}

	@Override
	public boolean collides() {
		return true;
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	@Override
	public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
	if (!world.isClient()) {
			dropItem();
		}
		return ActionResult.SUCCESS;
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		dropItem();
		return false;
	}

	public void dropItem() {
		dropStack(stored);
		world.playSound(null, getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1, 1);
		discard();
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {
		this.stored = ItemStack.fromNbt(nbt.getCompound("Item"));
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
		nbt.put("Item", this.stored.getNbt());
	}

	// ----------the actual important part ----------

	public ItemStack stored = ItemStack.EMPTY;

	@Override
	public void writeCustomSpawnData(PacketByteBuf buffer) {
		buffer.writeItemStack(this.stored);
	}

	@Override
	public void readCustomSpawnData(PacketByteBuf buffer) {
		this.stored = buffer.readItemStack();
	}
}
