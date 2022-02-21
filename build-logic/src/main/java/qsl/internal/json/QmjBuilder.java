package qsl.internal.json;

import org.quiltmc.json5.JsonWriter;
import qsl.internal.dependency.QslLibraryDependency;
import qsl.internal.extension.QslModuleExtensionImpl;

import java.io.IOException;
import java.nio.file.Path;

public final class QmjBuilder {
	public static void buildQmj(String version, String loaderVersion, String minecraftVersion, QslModuleExtensionImpl ext, Path path) throws IOException {
		JsonWriter writer = JsonWriter.json(path);
		// write everything that is always present
		writer.beginObject()
				.name("schema_version").value("1")
				.name("quilt_loader").beginObject() // root object -> quilt_loader
				.name("group").value("org.quiltmc.qsl." + ext.getLibrary().get())
				.name("id").value(ext.getId().get())
				.name("version").value(version)
				.name("metadata").beginObject() // quilt_loader -> metadata
				.name("name").value(ext.getName().get())
				.name("description").value(ext.getDescription().get())
				.name("contributors").beginObject() // metadata -> contributors
				.name("Owner").value("QuiltMC: QSL Team")
				.endObject() // contributors -> metadata
				.name("contact").beginObject() // contributors -> contact
				.name("homepage").value("https://quiltmc.org")
				.name("issues").value("https://github.com/quiltmc/quilt-standard-libaries/issues")
				.name("sources").value("https://github.com/quiltmc/quilt-standard-libraries")
				.endObject() // contact -> metadata
				.name("license").value("Apache-2.0")
				.name("icon").value("assets/" + ext.getId().get() + "/icon.png")
				.endObject(); // metadata -> quilt_loader

		writer.name("depends").beginObject() // quilt_loader -> depends
				.name("quilt_loader").value(loaderVersion)
				.name("minecraft").value(minecraftVersion);
		for (QslLibraryDependency depend : ext.getModuleDependencyDefinitions()) {
			writer.name(depend.getName()).value("*"); // TODO: depend on a semver version range
		}
		writer.endObject(); // depends -> quilt_loader

		if (!ext.getEntrypoints().isEmpty()) {
			writer.name("entrypoints").beginObject(); // quilt_loader -> entrypoints
			for (QslModuleExtensionImpl.EntrypointObjectHolder entrypoint : ext.getEntrypoints()) {
				writer.name(entrypoint.getName());
				writer.beginArray();
				for (String clazz : entrypoint.getValues().get()) {
					writer.value(clazz);
				}
				writer.endArray();
			}
			writer.endObject(); // entrypoints -> quilt_loader
		}
		writer.endObject(); // quilt_loader -> root object

		if (ext.getHasMixins().get()) {
			writer.name("mixin").value(ext.getId().get() + ".mixins.json");
		}

		if (ext.getHasAccessWidener().get()) {
			writer.name("access_widener").value(ext.getId().get() + ".accesswidener");
		}

		// TODO: environment
		if (ext.getEnvironment().get() != Environment.ANY) {
			writer.name("minecraft").beginObject() // root object -> minecraft
					.name("environment").value(ext.getEnvironment().get().qmj)
					.endObject(); // minecraft -> root object
		}
		writer.name("modmenu").beginObject() // root object -> modmenu
				.name("badges").beginArray().value("library").endArray()
				.name("parent").beginObject() // modmenu -> parent
				.name("id").value("qsl")
				.name("name").value("Quilt Standard Libraries")
				.name("description").value("A set of libraries to assist in making Quilt mods")
				.name("badges").beginArray().value("library").endArray()
				.endObject() // parent -> modmenu
				.endObject(); // modmenu -> root

		writer.endObject(); // end root object
		writer.flush();
		writer.close();
	}
}
