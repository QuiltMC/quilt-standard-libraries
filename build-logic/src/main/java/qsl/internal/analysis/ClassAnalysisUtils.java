package qsl.internal.analysis;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import net.fabricmc.loom.LoomGradleExtension;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import net.fabricmc.loom.configuration.providers.minecraft.mapped.NamedMinecraftProvider;
import net.fabricmc.loom.configuration.providers.minecraft.mapped.ProcessedNamedMinecraftProvider;
import org.gradle.api.Project;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Represents several class analysis utilities.
 */
public class ClassAnalysisUtils {
	public static ClassNode readClass(Path path) throws IOException {
		var node = new ClassNode();

		try (var is = Files.newInputStream(path)) {
			new ClassReader(is).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
		}

		return node;
	}

	public static Path getClassPath(FileSystem jarFs, String path) {
		return jarFs.getPath(path.replace('.', '/') + ".class");
	}

	public static Stream<ClassNode> readPackage(FileSystem jarFs, String targetPackage) throws IOException {
		return readPackage(Files.list(jarFs.getPath(targetPackage.replace('.', '/'))));
	}

	public static Stream<ClassNode> readPackageRecursively(FileSystem jarFs, String targetPackage) throws IOException {
		return readPackage(Files.walk(jarFs.getPath(targetPackage.replace('.', '/'))));
	}

	private static Stream<ClassNode> readPackage(Stream<Path> in) {
		return in
				.filter(path -> path.toString().endsWith(".class") && Files.isRegularFile(path))
				.map(path -> {
					try {
						return readClass(path);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				})
				.sorted(Comparator.comparing(classNode -> classNode.name));
	}

	public static FileSystem loadMinecraftJar(Project project) throws IOException {
		var namedMinecraftProvider = ((LoomGradleExtension) project.getExtensions().getByType(LoomGradleExtensionAPI.class))
				.getNamedMinecraftProvider();

		if (namedMinecraftProvider instanceof ProcessedNamedMinecraftProvider<?, ?> processed) {
			var provider = (NamedMinecraftProvider.MergedImpl) processed.getParentMinecraftProvider();
			return loadMinecraftJar(provider);
		} else if (namedMinecraftProvider instanceof NamedMinecraftProvider.MergedImpl provider) {
			return loadMinecraftJar(provider);
		}

		throw new IOException("Could not locate Minecraft merged JAR.");
	}

	private static FileSystem loadMinecraftJar(NamedMinecraftProvider.MergedImpl provider) throws IOException {
		Path inputJar = provider.getMergedJar().getPath();
		URI jarUri = URI.create("jar:" + inputJar.toUri());

		try {
			return FileSystems.getFileSystem(jarUri);
		} catch (FileSystemNotFoundException e) {
			return FileSystems.newFileSystem(jarUri, Map.of("create", false));
		}
	}

	public static Map<String, MethodNode> buildMethodLookup(ClassNode classNode) {
		return classNode.methods.stream().collect(HashMap::new, (map, node) -> {
			map.put(node.name + node.desc, node);
		}, HashMap::putAll);
	}
}
