/*
 * Copyright 2025 The Quilt Project
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

package org.quiltmc.qsl.resource.loader.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.stb.STBImage;

import com.mojang.blaze3d.texture.NativeImage;

@ApiStatus.Internal
public class NativeImageUtil {
	public static byte[] getBytes(NativeImage image) throws IOException {
		byte[] bytes;
		try (
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				WritableByteChannel writableByteChannel = Channels.newChannel(byteArrayOutputStream);
		) {
			if (!image.write(writableByteChannel)) {
				throw new IOException("Could not write image to byte array: " + STBImage.stbi_failure_reason());
			}

			bytes = byteArrayOutputStream.toByteArray();
		}

		return bytes;
	}
}
