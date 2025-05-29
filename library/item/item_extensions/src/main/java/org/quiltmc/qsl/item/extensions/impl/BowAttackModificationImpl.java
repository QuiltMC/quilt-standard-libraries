package org.quiltmc.qsl.item.extensions.impl;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.item.extensions.api.bow.BowShotProjectileEvents;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;

@ApiStatus.Internal
public class BowAttackModificationImpl {
    public static PersistentProjectileEntity modifyShotProjectile(
        PersistentProjectileEntity originalProjectile,
        ItemStack arrowStack, float pullProgress, ItemStack bowStack, LivingEntity user
    ) {
        final PersistentProjectileEntity replacedPersistentProjectileEntity = BowShotProjectileEvents
            .BOW_REPLACE_SHOT_PROJECTILE.invoker()
            .replaceProjectileShot(bowStack, arrowStack, user, pullProgress, originalProjectile);

        BowShotProjectileEvents.BOW_MODIFY_SHOT_PROJECTILE.invoker().modifyProjectileShot(
            bowStack, arrowStack, user, pullProgress, replacedPersistentProjectileEntity
        );

        return replacedPersistentProjectileEntity;
    }
}
