/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.tracked_data.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.tracked_data.impl.QuiltTrackedDataInitializer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TrackedDataHandlerRegistry.class)
public class TrackedDataHandlerRegistryMixin {
	@Unique
	private static final Logger quilt$LOGGER = LogUtils.getLogger();
	@Unique
	private static int quilt$currentUnknownId = 0;

	/**
	 * @author Patbox
	 */
	@Overwrite
	public static int getId(TrackedDataHandler handler) {
		return QuiltTrackedDataInitializer.HANDLER_REGISTRY.getRawId(handler);
	}

	/**
	 * @author Patbox
	 */
	@Overwrite
	public static TrackedDataHandler get(int id) {
		return QuiltTrackedDataInitializer.HANDLER_REGISTRY.get(id);
	}

	@Inject(method = "register", at = @At("HEAD"))
	private static void quilt$register(TrackedDataHandler<?> handler, CallbackInfo ci) {
		String id;

		if (handler == TrackedDataHandlerRegistry.BYTE) {
			id = "byte";
		} else if (handler == TrackedDataHandlerRegistry.INTEGER) {
			id = "integer";
		} else if (handler == TrackedDataHandlerRegistry.FLOAT) {
			id = "float";
		} else if (handler == TrackedDataHandlerRegistry.STRING) {
			id = "string";
		} else if (handler == TrackedDataHandlerRegistry.TEXT_COMPONENT) {
			id = "text_component";
		} else if (handler == TrackedDataHandlerRegistry.OPTIONAL_TEXT_COMPONENT) {
			id = "optional_text_component";
		} else if (handler == TrackedDataHandlerRegistry.ITEM_STACK) {
			id = "item_stack";
		} else if (handler == TrackedDataHandlerRegistry.BOOLEAN) {
			id = "boolean";
		} else if (handler == TrackedDataHandlerRegistry.ROTATION) {
			id = "rotation";
		} else if (handler == TrackedDataHandlerRegistry.BLOCK_POS) {
			id = "block_pos";
		} else if (handler == TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS) {
			id = "optional_block_pos";
		} else if (handler == TrackedDataHandlerRegistry.FACING) {
			id = "facing";
		} else if (handler == TrackedDataHandlerRegistry.OPTIONAL_UUID) {
			id = "optional_uuid";
		} else if (handler == TrackedDataHandlerRegistry.OPTIONAL_BLOCK_STATE) {
			id = "optional_block_state";
		} else if (handler == TrackedDataHandlerRegistry.TAG_COMPOUND) {
			id = "tag_compound";
		} else if (handler == TrackedDataHandlerRegistry.PARTICLE) {
			id = "particle";
		} else if (handler == TrackedDataHandlerRegistry.VILLAGER_DATA) {
			id = "villager_data";
		} else if (handler == TrackedDataHandlerRegistry.FIREWORK_DATA) {
			id = "firework_data";
		} else if (handler == TrackedDataHandlerRegistry.ENTITY_POSE) {
			id = "entity_pose";
		} else if (handler == TrackedDataHandlerRegistry.CAT_VARIANT) {
			id = "cat_variant";
		} else if (handler == TrackedDataHandlerRegistry.FROG_VARIANT) {
			id = "frog_variant";
		} else if (handler == TrackedDataHandlerRegistry.OPTIONAL_GLOBAL_POSITION) {
			id = "optional_global_position";
		} else if (handler == TrackedDataHandlerRegistry.PAINTING_VARIANT) {
			id = "painting_variant";
		} else {
			id = "unknown_handler/" + (quilt$currentUnknownId++);
			if (QuiltLoader.isDevelopmentEnvironment()) {
				quilt$LOGGER.warn("Detected registration of unknown TrackedDataHandler through vanilla method! Object: {}, Class: {}", handler.toString(), handler.getClass().getName());
				for (StackTraceElement traceElement : Thread.currentThread().getStackTrace()) {
					quilt$LOGGER.warn("\tat " + traceElement);
				}
			}
		}

		Registry.register(QuiltTrackedDataInitializer.HANDLER_REGISTRY, new Identifier(id), handler);
	}
}
