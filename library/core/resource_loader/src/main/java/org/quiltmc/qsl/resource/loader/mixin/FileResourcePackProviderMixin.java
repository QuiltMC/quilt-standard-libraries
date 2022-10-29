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

package org.quiltmc.qsl.resource.loader.mixin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.resource.pack.FileResourcePackProvider;

@Mixin(FileResourcePackProvider.class)
public class FileResourcePackProviderMixin {
	/*
	 * This injection is a bug fix!
	 *
	 * The issue is Mojang unconditionally attempts to create the resource pack directory, the method doesn't throw if the directory exists.
	 * But the issue are symlinks: if it's a symlink instead, the method fails (as it cannot create a directory).
	 *
	 * This is fixed by adding additional checks.
	 */
	@Redirect(
			method = "register",
			at = @At(
					value = "INVOKE",
					target = "Ljava/nio/file/Files;createDirectories(Ljava/nio/file/Path;[Ljava/nio/file/attribute/FileAttribute;)Ljava/nio/file/Path;"
			)
	)
	private Path onCreateResourcePackDirectory(Path path, FileAttribute<?>[] attrs) throws IOException {
		if (Files.isDirectory(path)) {
			// It's already a directory, and it followed the symlink so all good.
			return path;
		} else if (Files.isSymbolicLink(path)) {
			// So, there *is* a symbolic link! Let's read it.
			Path symbolicLink = Files.readSymbolicLink(path);

			// Uh? The symbolic link points to something that exists but isn't a directory? Time to give up.
			if (Files.exists(symbolicLink)) {
				throw new IOException("Could not create nor read resource pack directory: the symbolic link points to a file instead of a directory.");
			}

			// Let's attempt to create the directory at the place the symbolic link pointed at instead.
			path = symbolicLink;
		}

		return Files.createDirectories(path, attrs);
	}
}
