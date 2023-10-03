/*
 * Copyright 2023 The Quilt Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.registry.mixin.compat;

import com.mojang.serialization.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.unmapped.C_dfpyqayl;

@Mixin(C_dfpyqayl.class)
public abstract class C_dfpyqaylMixin {
	@Shadow
	private static <T> Dynamic<T> method_53085(Dynamic<T> dynamic, String string, Dynamic<T> dynamic2, String string2) {
		return null;
	}

	@Unique
	private static final String EFFECT_ID_KEY = "quilt:effect_id";
	@Unique
	private static final String VANILLA_EFFECT_ID = "id";
	@Unique
	private static final String STATUS_EFFECT_INSTANCE_ID_KEY = "quilt:id";
	@Unique
	private static final String QUILT_BEACON_PRIMARY_EFFECT_KEY = "quilt:primary_effect";
	@Unique
	private static final String QUILT_BEACON_SECONDARY_EFFECT_KEY = "quilt:secondary_effect";
	@Unique
	private static final String VANILLA_BEACON_PRIMARY_EFFECT_KEY = "primary_effect";
	@Unique
	private static final String VANILLA_BEACON_SECONDARY_EFFECT_KEY = "secondary_effect";

	@Unique
	private static Dynamic<?> migrateId(Dynamic<?> dynamic, String fromKey, Dynamic<?> dynamic2, String toKey) {
		dynamic.get(fromKey).result().ifPresent(value -> {
			dynamic2.set(toKey, value);
			dynamic2.remove(fromKey);
		});

		return dynamic2;
	}

	@Inject(
			method = "method_53108",
			at = @At("RETURN"),
			cancellable = true
	)
	private static void addQuiltBeaconFixer(Dynamic<?> _dynamic, CallbackInfoReturnable<Dynamic<?>> cir) {
		Dynamic<?> dynamic = cir.getReturnValue();
		dynamic = migrateId(dynamic, QUILT_BEACON_PRIMARY_EFFECT_KEY, dynamic, VANILLA_BEACON_PRIMARY_EFFECT_KEY);
		dynamic = migrateId(dynamic, QUILT_BEACON_SECONDARY_EFFECT_KEY, dynamic, VANILLA_BEACON_SECONDARY_EFFECT_KEY);
		cir.setReturnValue(dynamic);
	}

	@Redirect(
			method = "method_53083",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/unmapped/C_dfpyqayl;method_53085(Lcom/mojang/serialization/Dynamic;Ljava/lang/String;Lcom/mojang/serialization/Dynamic;Ljava/lang/String;)Lcom/mojang/serialization/Dynamic;"
			)
	)
	private static <T> Dynamic<T> addEntityAndSuspiciousStewFixer(Dynamic<T> dynamic, String string, Dynamic<T> dynamic2, String string2) {
		dynamic2 = method_53085(dynamic, "EffectId", dynamic2, VANILLA_EFFECT_ID); // Read Vanilla Fixer
		dynamic2 = (Dynamic<T>) migrateId(dynamic, EFFECT_ID_KEY, dynamic2, VANILLA_EFFECT_ID);

		return dynamic2;
	}

	@ModifyVariable(
			method = "method_53082",
			at = @At(
				value = "INVOKE_ASSIGN",
				target = "Lnet/minecraft/unmapped/C_dfpyqayl;method_53096(Lcom/mojang/serialization/Dynamic;Ljava/lang/String;Ljava/lang/String;)Lcom/mojang/serialization/Dynamic;"
			),
			argsOnly = true
	)
	private static <T> Dynamic<T> addStatusEffectFixer(Dynamic<T> value) {
		return (Dynamic<T>) migrateId(value, STATUS_EFFECT_INSTANCE_ID_KEY, value, VANILLA_EFFECT_ID);
	}
}
