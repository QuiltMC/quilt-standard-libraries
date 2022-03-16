/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.registry.attachment.test.client;

import net.fabricmc.loader.api.ModContainer;

import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;

public class ClientAttachmentTest implements ClientModInitializer {
	public static final RegistryEntryAttachment<Block, Boolean> BASED =
			RegistryEntryAttachment.boolBuilder(Registry.BLOCK, new Identifier("quilt", "based"))
					.side(RegistryEntryAttachment.Side.CLIENT).build();

	@Override
	public void onInitializeClient(ModContainer mod) { }
}
